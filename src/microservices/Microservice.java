package microservices;

/**
 * A microservice which will recieve some bytes, perform a service with those bytes,
 * and return a response in the form of some bytes.
 * @author Joel Happ
 *
 */
public interface Microservice {
	/**
	 * @return The name of this Microservice
	 */
	public String name();
	
	/**
	 * Performs a microservice
	 */
	public byte[] performService(byte[] b);
	
}
