package microservices;
/**
 * A string service which does nothing but echo the original string that gets sent to it,
 * with no changes.
 * @author Joel Happ
 */
public class EchoService extends StringMicroservice{

	@Override
	public String name() {
		return "Echo";
	}

	/**
	 * Returns the exact string that it was passed
	 */
	public String stringService(String s) {
		return s;
	}
}
