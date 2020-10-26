package transport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	// Server's port
	private Scanner scanner;
	private Socket socket;
	
	//Input and output streams
	private DataOutputStream dout;
	private DataInputStream din;
	
	
	// Message Info
	private byte[] microserviceTransforms; // The sequence of bytes which represents which microservices to apply
	private byte[] stringMessage;
	
	
    private Client(String destinationAddr, int serverPort) throws IOException {
        
    	// Retrieves server address, sets socket address
    	InetAddress serverAddress = InetAddress.getByName(destinationAddr);
        //this.address = new InetSocketAddress(serverAddress, serverPort);
        
        // Creates socket to connect to server
        socket = new Socket(serverAddress, serverPort);
        
        // Output and input streams of socket
		dout = new DataOutputStream(socket.getOutputStream());
		din = new DataInputStream(socket.getInputStream());
    }
	
	
	public static void main(String [] args) {
		Client client;
		try {
		 client = new Client(args[0], Integer.parseInt(args[1]));
        System.out.println("Started Client at " + InetAddress.getLocalHost() + " Port: " + Integer.parseInt(args[1]) );
		client.start();
		}catch(Exception e){
			System.err.println("Could not start client, exiting program..." + e);
			System.exit(1);
		}

	}
	
	/**
	 * Commences client, continuously calls menu to get user input
	 */
	public void start() {
		try {
		    scanner = new Scanner(System.in);
	        while (true) {
	        	menu();
	        }
		}catch(Exception e) {
			System.err.println("Error: " + e.getStackTrace().toString());
		}
	}
	
	/**
	 * Prints some options for user input
	 */
	public void displayOptions() {
		System.out.println("Select 1 of the following options by typing an integer and hitting enter: ");
		System.out.println("1: Select Microservices you would like to use");
		System.out.println("2: Set the string you would like to perform microservices on");
		System.out.println("3: Send request to server to perform selected microservices with current string");
		System.out.println("4: Start or stop microservices");
		System.out.println("5: Quit");
	}
	
	/**
	 * Displays a menu, responds accordingly
	 * @throws Exception
	 */
	public void menu() throws Exception {
		displayOptions();
		String in = scanner.nextLine();
		try {
			int res = Integer.parseInt(in);
			switch(res) {
			case 1:
				selectMicroservices();
				break;
			case 2:
				selectString();
				break;
			case 3:
				sendMessage();
				break;
			case 4:
				manageMicroservices();
				break;
			case 5:
				quit();
				break;
			default:
				System.err.println("You entered an invalid input - please try again");
			}
		} catch(NumberFormatException e) {
			System.err.println("Could not interpret your input - please try again");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * The client selects which microservice numbers they want to be performed on their string
	 * @throws Exception
	 */
	public void selectMicroservices() throws Exception{
		System.out.println("Enter a sequence of integers (Max 10) to represent the microservices you wish to apply.");
		System.out.println("For example, 1542 will apply Echo, Caesar, Lower, and Reverse in that order");
		
		String in = scanner.nextLine().trim();
		// Converts the string into a char array and ensures it was a valid sequence of numbers
		char [] selected = ("" + Integer.parseInt(in)).toCharArray();
		if(selected.length > 10) {
			throw new Exception("Your sequence of integers cannot be more than 10 long");
		}
		
		microserviceTransforms = new byte[selected.length];
		for(int i = 0; i<selected.length; i++) {
			microserviceTransforms[i] = new Integer(Integer.parseInt(String.valueOf(selected[i]))).byteValue();
			//System.out.println(microserviceTransforms[i]);
		}
	}
	
	/**
	 * Tells the server to start or stop microservices
	 * @throws Exception
	 */
	public void manageMicroservices() throws Exception{
		System.out.println("Enter 1 to start a microservice. Enter 0 to stop a microservice");
		int res = Integer.parseInt(scanner.nextLine().trim());
		if((res == 1)||(res==0)) {
			String command = (res==1) ? "start" : "stop";
			
			int service = 0;
			
			System.out.println("Enter a number between 1 and 9 where you would like to " + command + " the microservice:");
			int port = Integer.parseInt(scanner.nextLine().trim());
			if(port<1 || port > 9) {
				throw new Exception("Your input was invalid.");
			}
			if(res == 1) {
				System.out.println("Choose a number between 1 and 6 to represent the service run by your microservice server (indexed according to assignment description)");
				service = Integer.parseInt(scanner.nextLine().trim());
				if(service<1 || service > 6) {
					throw new Exception("Your input was invalid.");
				}
			}
			
			byte [] options = new byte[3];
			options[0] = new Integer(res).byteValue(); // Stop (0) or start (1)
			options[1] = new Integer(port).byteValue(); // Port offset
			if(res==1)
			options[2] = new Integer(service).byteValue(); //
			
			sendPacket(new TCPPacket(TCPPacket.CONFIG, options, new byte[1]));
			
			
		} else if (res == 0) {
			// Stop a microservice
			
		} else {
			throw new Exception("Your input was invalid.");
		}
		
	}
	
	/**
	 * The client selects which string they want the services to be performed on
	 * @throws Exception
	 */
	public void selectString() throws Exception {
		System.out.println("Enter a string which you would like to perform operations on: ");
		String in = scanner.nextLine().trim();
		byte [] b = in.getBytes();
		if(b.length < 200) {
			stringMessage = b;
		}
		else {
			System.err.println("The message you entered was too long. Please try again.");
		}
	}
	
	/**
	 * The client sends a packet to the server with the currently selected services and content, and recieves a reply
	 * @throws Exception
	 */
	public void sendMessage() throws Exception{
		//System.out.println("Sending message: " + stringMessage + " with commands:" + microserviceTransforms);
		
		sendPacket(new TCPPacket(TCPPacket.SEND_REQ, microserviceTransforms, stringMessage));

		TCPPacket response;
		while(true) {
			response = recievePacket();
			if(TCPPacket.matches(response.getHeader(), TCPPacket.SEND_RES)) {
				break;
			}
		}
		
		String resContent = new String(response.getContent());
		System.out.println("Message recieved!\n" + resContent +"\n Would you like to save the message to perform further operations? Enter 1 for yes, enter anything else for no.");
		String in = scanner.nextLine().trim();
		if(Integer.parseInt(in)==1) {
			stringMessage = response.getBytes();
		}
	}
	
	/**
	 * Sends given packet to the server
	 * @param p TCPPacket to send
	 */
	public void sendPacket(TCPPacket p) {
		try {
		dout.writeInt(TCPPacket.SIZE);
		dout.write(p.getBytes());
		} catch(Exception e){
			System.err.println("Error sending packet - " +e.getMessage());
		}
	}
	
	
	/**
	 * Receives a TCPPacket from the server
	 * @return THe TCPPacket that it received
	 */
	public TCPPacket recievePacket() {
		while(true) {
			try {
			int length = din.readInt();                    // read length of incoming message
			if(length>0) {
			    byte[] message = new byte[length];
			    din.readFully(message, 0, message.length);
			    return new TCPPacket(message);
			}
			} catch (Exception e) {
				System.err.println("Error recieving packet - " +e.getMessage());
			}
		}
	}
	
	/**
	 * Terminates this client
	 */
	public void quit() {
		byte[] blank = new byte[1];
		sendPacket(new TCPPacket(TCPPacket.DISCONNECT, blank, blank));
		
		try {
			scanner.close();
			socket.close();
		} catch(Exception e) {
			System.err.println("Unable to properly close everything.. ");
			System.exit(1);
		}
		System.out.println("Terminating client.");
		System.exit(0);
	}
	
	
	
	
}
