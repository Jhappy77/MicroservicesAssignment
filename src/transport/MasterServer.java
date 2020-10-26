package transport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import microservices.CaesarService;
import microservices.EchoService;
import microservices.LowerService;
import microservices.Microservice;
import microservices.ReverseService;
import microservices.UpperService;
import microservices.YourService;

public class MasterServer implements Runnable {
	private Socket socket;
	private static UDPServer[] microservices;
	private static Thread[] microthreads;
	private UDPClient microClient;
	DataOutputStream output;
	DataInputStream input;
	
	// Port Number for the next UDPClient connection to use
	// (In a real application, would need a better system)
	private static int nextUdpClientPort = 9000;
	// A flag to decide if the current MasterServer thread should quit
	private volatile boolean shouldQuit = false;
	
	
	public static void main(String[] args) {
		runServer(Integer.parseInt(args[0]));
	}
	
	/**
	 * Begins a master service, using the connected socket as the server endpoint
	 * @param socket Socket to use as endpoint
	 */
	public MasterServer(Socket socket) {
		this.socket = socket;
		startMicroserviceClient();
	}
	
	/**
	 * Called by main in order to boot up Microservices and the ServerSocket to accept connections.
	 * @param pnum Port number to host ServerSocket on
	 */
	public static void runServer(int pnum) {
		try (ServerSocket s = new ServerSocket(pnum)){
		System.out.println("Started a Master Server at port " + pnum);
		startMicroservices();
		while(true) {
			Socket sock = s.accept();
			System.out.println("Server accepted a connection from " + sock.getInetAddress().getHostAddress());
			new Thread(new MasterServer(sock)).start();
		}
		} catch(Exception e) {
			System.err.println(e);
		}
	}
	

	/**
	 * Starts a bunch of microservice servers based on their default number mappings
	 * at ports 8001 - 8006.
	 */
	public static void startMicroservices() {
		microthreads = new Thread[9];
		microservices = new UDPServer[9];
		for(int i=1; i<7; i++) {
			startIndividualMicroservice(i, getServiceByNumber(i));
		}
	}
	
	/**
	 * Default number mappings of microservices, as specified by assignment description
	 * @param i 
	 * @return
	 */
	private static Microservice getServiceByNumber(int i) {
		switch(i) {
			case(1):
				return new EchoService();
			case(2):
				return new ReverseService();
			case(3):
				return new UpperService();
			case(4):
				return new LowerService();
			case(5):
				return new CaesarService();
			case(6):
				return new YourService();
			default:
				return new EchoService();
		}
	}
	
	/**
	 * Starts a specified microservice with a specified number
	 * @param i Number of the microservice. Should be a number between 1 and 9.
	 * @param m The microservice to start at the udpserver
	 */
	private static void startIndividualMicroservice(int i, Microservice m) {
		int port = 8000 + i;
		if(i>0 && i<10) {
			microservices[i] = new UDPServer(port, m);
			microthreads[i] = new Thread(microservices[i]); 
			microthreads[i].start();
		} else {
			System.err.println("Could not start individual microservice at port " + port);
		}
	}
	
	/**
	 * Starts a serverside UDPClient for interfacing with the microservices
	 */
	public void startMicroserviceClient() {
		try {
			microClient = new UDPClient(nextUdpClientPort++);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			quit();
		}
	}
	

	/**
	 * Runs the MasterServer, checks to see if it should be terminated
	 */
	@Override
	public void run() {
		try {
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(socket.getInputStream());
			while(true) {
				try{
					if(shouldQuit) {
						break;
					}
					respond();
				}
				catch(Exception e) {
					System.err.println("Error while running: " + e.getLocalizedMessage());
					break;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Server waits for a packet to be delivered by client, commences appropriate
	 * response based on the packet header
	 * @throws Exception
	 */
	public void respond() throws Exception{
		TCPPacket incoming = recievePacket();
		//System.out.println("Recieved Packet - Header:" + incoming.getHeader());
		
		byte [] h = incoming.getHeader();
		if(TCPPacket.matches(h, TCPPacket.SEND_REQ)) {
			respondToServiceRequest(incoming);
		} else if(TCPPacket.matches(h, TCPPacket.DISCONNECT)) {
			quit();
		} else if(TCPPacket.matches(h, TCPPacket.CONFIG)) {
			configMicroservices(incoming);
		} else {
			System.err.println("Error: unrecognized packet header received");
		}
		
	}
	
	/**
	 * The case where a packet has been sent in order to perform services 
	 * with the microservices
	 * @param p The packet with the relevant information
	 */
	private void respondToServiceRequest(TCPPacket p) {
		byte[] commands = p.getOptions();
		
		// Transforms the content according to each microservice's transforms
		byte [] content = p.getContent();
		for(int i = 0; i<commands.length; i++) {
			if(commands[i] > 0 && commands[i] < 10) {
				content = sendToMicroservice(content, commands[i]);
			}
		}
		
		// Sends TCPPacket with new content
		TCPPacket outgoing = new TCPPacket(TCPPacket.SEND_RES, new byte[1], content);
		sendPacket(outgoing);
	}
	
	/**
	 * Sends some bytes to a microservice, returns the result
	 * @param toSend The bytes to send to microservice
	 * @param serviceNumber The service number of the microservice (numbered 1-6, according to assignment guidelines)
	 * @return The resulting bytes from microservice, or the original bytes if microservice couldn't be called
	 */
	public byte[] sendToMicroservice(byte[] toSend, int serviceNumber) {
		try {
		//return microClient.callService(toSend, microservices[serviceNumber].getSocketAddress());
			return microClient.callService(toSend, microservices[serviceNumber].getAddress(), microservices[serviceNumber].getPort());
		} catch (Exception e) {
			System.err.println("Could not send to microservice with number " + serviceNumber + " - returning default");
			e.printStackTrace();
			return toSend;
		}
	}
	
	
	/**
	 * Sends a TCPPacket to the socket connected to this server
	 * @param p Packet to send
	 */
	public void sendPacket(TCPPacket p) {
		try {
		output.writeInt(TCPPacket.SIZE);
		output.write(p.getBytes());
		} catch(Exception e){
			System.err.println("Error sending packet - " +e.getMessage());
		}
	}
	
	/**
	 * Receives a packet from the client, returns it
	 * @return A TCPPacket sent from the client
	 */
	public TCPPacket recievePacket() {
		while(true) {
			try {
			int length = input.readInt();                    // read length of incoming message
			if(length>0) {
			    byte[] message = new byte[length];
			    input.readFully(message, 0, message.length);
			    return new TCPPacket(message);
			}
			} catch (Exception e) {
				System.err.println("Error recieving packet - " +e.getMessage());
			}
		}
	}
	
	/**
	 * Terminates the MasterServer
	 */
	private void quit(){
		try {
		output.close();
		input.close();
		microClient.close();
		socket.close();
		} catch(Exception e) {
			System.err.println("Error terminating socket..." + e.getMessage());
		}
		System.out.println("Terminating connection with a client");
		shouldQuit = true;
		//Thread.currentThread().interrupt();
		//System.exit(0);
	}
	
	/**
	 * Turns on or off microservices based on passed packet
	 * @param packet
	 */
	private void configMicroservices(TCPPacket packet) {
		byte[] options = packet.getOptions();
		if(options[0] == 0) {
			// Quit service
			terminateService(options[1]);
		} else if(options[0] == 1) {
			// Create new microservice
			terminateService(options[1]);
			startIndividualMicroservice(options[1], getServiceByNumber(options[2]));
		}
	}
	
	/**
	 * Turns of microservice numbered i
	 * (Interrupts thread and closes UDPServer)
	 * @param i Service number (index in microservices and microthreads array)
	 */
	private static void terminateService(int i) {
		if(microservices[i] != null) {
			System.out.println("Stopping service #"+i);
		microservices[i].stop();
		microthreads[i].interrupt();
		}
		else {
			System.out.println("No service to terminate at port " + i+8000);
		}
		if(microthreads[i] != null) {
			microthreads[i].interrupt();
			System.out.println("Interrupting Microservice thread #"+i);
		}else {
			System.out.println("No microservice thread to interrupt at thread #"+i);
		}
		
	}
	
	
	
}
