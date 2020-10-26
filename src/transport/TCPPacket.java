package transport;

public class TCPPacket {

	// When the client is disconnecting from server
	public static byte [] DISCONNECT = {1, 1, 1};
	// When the client is sending a string to server to perform service with it
	public static byte [] SEND_REQ = {0, 0, 1};
	// When the server is sending a response to the client with the transforms added
	public static byte [] SEND_RES = {0, 1, 0};
	// Configure microservices
	public static byte [] CONFIG = {1, 0, 0};
	
	// Size of the TCPPacket
	public static int SIZE = 400;
	
	private byte[] packet;
	
	
	public static boolean matches(byte[] b1, byte[] b2) {
		if(b1.length != b2.length) {
			return false;
		}
		for(int i=0; i<b1.length; i++) {
			if(b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}
	
	// Packet Format:
	// [0:2] -- HEADER, describes action to take with packet
	// [10:20] -- OPTIONS, contains bits which encode options for some specific headers
	// [50:350] -- CONTENT, encodes content which needs to be passed 
	// All other bits -- DON'T CARE, left open for design purposes (if more needs to be added)
	public TCPPacket(byte[] header, byte[] options, byte[] content) {
		packet = new byte[400];
		
		if(header.length != 3) {
			System.err.println("warning - invalid header");
		}
		
		int optionsLength = options.length < 11 ? options.length: 10;
		int contentLength = content.length < 301 ? content.length : 300;
		
		System.arraycopy(header, 0, packet, 0, 3);
		System.arraycopy(options, 0, packet, 10, optionsLength);
		System.arraycopy(content, 0, packet, 50, contentLength);
		
	}
	
	public TCPPacket(byte[] b) {
		if(b.length != 400) {
			System.err.println("warning - should not be making packet with size " + b.length);
		}
		packet = b;
	}
	
	public byte[] getBytes() {
		return packet;
	}
	
	public byte[] getHeader() {
		byte [] b = new byte[3];
		System.arraycopy(packet, 0, b, 0, 3);
		return b;
	}
	
	public byte[] getContent() {
		byte [] b = new byte[300];
		System.arraycopy(packet, 50, b, 0, 300);
		return b;
	}
	
	public byte[] getOptions() {
		byte [] b = new byte[10];
		System.arraycopy(packet, 10, b, 0, 10);
		return b;
	}
	
}
