package microservices;
/**
 * A Microservice which expects to recieve a string, perform some operation on it, then return a string
 * @author Joel Happ
 */
public abstract class StringMicroservice implements Microservice{
	
	public byte[] performService(byte[] b) {
		String msg = new String(b).trim();
		String resp = stringService(msg);
		return resp.getBytes();
	}
	
	public abstract String stringService(String s);
	
}
