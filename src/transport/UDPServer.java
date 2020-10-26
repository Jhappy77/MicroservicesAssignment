package transport;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import microservices.Microservice;

public class UDPServer implements Runnable{
	private int port_num;
	private DatagramSocket socket;
	private Microservice service;
	
//	public static void main(String[] args) throws Exception{
//		UDPServer server = new UDPServer(Integer.parseInt(args[0]));
//		server.listen();
//	}
	
	public UDPServer(int p, Microservice s) {
		service = s;
		port_num = p;
		try {
			socket = new DatagramSocket(port_num);
		} catch (SocketException e) {
			System.err.println("Could not start microservice-- ");
			e.printStackTrace();
		}
	}
	
	/**
	 * Server listens for messages and sends responses
	 * @throws Exception
	 */
	private void listen() throws Exception{
		System.out.println("Started UDP socket with service: " + service.name() + " at port " + port_num + " at " + InetAddress.getLocalHost());
		
        byte[] respBytes;
        
        while (true) {
            
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            // Waits until a packet is recieved
            socket.receive(packet);
            
            // Performs a service with the bytes from the packet. Resp contains response.
            respBytes = service.performService(packet.getData());
            
            // Address of where incoming packet was sent from - and return address for outgoing
            SocketAddress address = packet.getSocketAddress();

            // Send the response back
            DatagramPacket sendPacket = new DatagramPacket(respBytes, respBytes.length, address);
            socket.send(sendPacket);
        }
		
	}
	
	/**
	 * Returns the address of this UDP server as a string
	 * @return the address of this UDP server as a string
	 */
	public InetAddress getAddress() {
		return socket.getLocalAddress();
	}
	
	/**
	 * Returns the socket address this UDP Server is bound to
	 * @return the socket address this UDP Server is bound to
	 */
	public SocketAddress getSocketAddress() {
		return socket.getLocalSocketAddress();
	}
	
	/**
	 * Returns the port number of this UDP server
	 * @return the port number of this UDP server
	 */
	public int getPort() {
		return port_num;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			listen();
		} catch (NullPointerException e) {
			System.err.println("Terminating server..");
		}catch (Exception e) {
			System.err.println("Error in UDP Server @" + getAddress().getHostName() + " port: " + port_num);
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			socket.close();
		} catch(Exception e){
			System.err.println("error closing socket");
		}
	}

	
}
