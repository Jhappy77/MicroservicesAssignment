package microservices;

/**
 * A string service which reverses the content of the original string
 * @author Joel Happ
 */
public class ReverseService extends StringMicroservice{

	@Override
	public String name() {
		return "Reverse";
	}

	/**
	 * Reverses the order of characters in the string that is passed in, returns the result
	 */
	public String stringService(String s) {
		char[] ca = s.toCharArray();
		int j = ca.length - 1;
		for(int i=0; i<j; i++) {
			char temp = ca[i];
			ca[i] = ca[j];
			ca[j] = temp;
			j--;
		}
		return new String(ca);
	}

}
