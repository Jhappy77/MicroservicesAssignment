package microservices;

/**
 * A string service which capitalizes every alphabetical char in the string
 * @author Joel Happ
 */
public class UpperService extends StringMicroservice{

	@Override
	public String name() {
		return "Upper";
	}

	/**
	 * Capitalizes every lower case letter in the string, returns result
	 */
	public String stringService(String s) {
		char[] ca = s.toCharArray();
		for(int i=0; i<ca.length; i++) {
			if(Character.isAlphabetic(ca[i]) && Character.isLowerCase(ca[i])) {
				ca[i] = Character.toUpperCase(ca[i]);
			}
		}
		return new String(ca);
	}

}
