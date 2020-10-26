package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPClient {
	
    private DatagramSocket udpSocket;
    private int port;
    
    /**
     * Constructs a UDPClient at given port number
     * @param port Port number to bind UDPClient to
     * @throws IOException
     */
    public UDPClient(int port) throws IOException {
        this.port = port;
        udpSocket = new DatagramSocket(this.port);
    }
    
    /**
     * Calls a microservice using UDP to the passed socket addresss
     * @param b Bytes to send to microservice
     * @param sa Address to send them to
     * @return Transformed bytes, or the original bytes if the microservice could not be contacted
     */
    public byte[] callService(byte[] b, InetAddress i, int port){
    	try {
    		i = InetAddress.getByName("localhost");
    	DatagramPacket p = new DatagramPacket(
                b, b.length, i, port);
    	// Timeout Limit of 4seconds
    	udpSocket.setSoTimeout(4000);
    	udpSocket.send(p);
    	return udpRecieve();
    	} catch (SocketTimeoutException e) {
    		System.err.println("Was unable to contact UDP Service at port " + port + ", performing no service");
    		return b;
    	}   catch(Exception e) {
    		System.out.println("Call to microservice at address " + i.toString() + port + " was unsuccessful");
    		e.printStackTrace();
    		return b;
    	}
    }
    
    /**
     * Receives a packet from a UDP Server
     * @return The byte content of the received packet
     * @throws IOException If there was an error communicating with the UDP Server
     */
    private byte[] udpRecieve() throws IOException {
        byte[] buf = new byte[300];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        udpSocket.receive(packet);
        // Trims extra spaces
        return new String(packet.getData()).trim().getBytes();
    }
    
    /**
     * Closes this UDPClient's socket
     */
    public void close() throws IOException {
    	udpSocket.close();
    }
    
}