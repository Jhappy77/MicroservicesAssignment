package microservices;

/**
 * A string service which lowercases every alphabetical char in the string
 * @author Joel Happ
 */
public class LowerService extends StringMicroservice{

	@Override
	public String name() {
		return "Lower";
	}

	/**
	 * Decapitalizes every upper case letter in the string, returns result
	 */
	public String stringService(String s) {
		char[] ca = s.toCharArray();
		for(int i=0; i<ca.length; i++) {
			if(Character.isAlphabetic(ca[i]) && Character.isUpperCase(ca[i])) {
				ca[i] = Character.toLowerCase(ca[i]);
			}
		}
		return new String(ca);
	}

}