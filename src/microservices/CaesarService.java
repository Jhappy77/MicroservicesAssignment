package microservices;

/**
 * A string service that applies a Caesar Cipher of a 2-character shift to
 * every alphabetical character that it recieves
 * @author Joel Happ
 */
public class CaesarService extends StringMicroservice{

	@Override
	public String name() {
		return "Caesar";
	}

	/**
	 * Applies Caesar shift of 2 to every character, returns result
	 */
	public String stringService(String s) {
		char[] ca = s.toCharArray();
		for(int i=0; i<ca.length; i++) {
			if(Character.isAlphabetic(ca[i])) {
				ca[i] = applyCaesarShift(ca[i], 2);
			}
		}
		return new String(ca);
	}
	
	/**
	 * Applies caesar shift to an individual character
	 * @param c Character to apply shift to
	 * @param i Amount to shift by
	 * @return Shifted character
	 */
	public char applyCaesarShift(char c, int i) {
		i %= 26;
		if(Character.isLowerCase(c)) {
			c += i;
			if(c > 'z') {
				c -= 26;
			}
			return c;
		} else {
			// Assume character is upper case
			c += i;
			if(c > 'Z') {
				c -= 26;
			}
			return c;
		}
	}

}