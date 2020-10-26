package microservices;

/**
 * My custom microservice for the project (UwU Service)
 * A string service which UwUfies the String content
 * (Changes every r to a w, every l to a w, and every 'ee' sequence to an 'ii' sequence)
 * @author Joel Happ
 */
public class YourService extends StringMicroservice{

	@Override
	public String name() {
		return "Yours (UwU)";
	}

	/**
	 * UwU-fies the string content
	 */
	public String stringService(String s) {
		char[] ca = s.toCharArray();
		
		boolean wasE = false;
		
		for(int i=0; i<ca.length; i++) {
			char ch = ca[i];
			if(Character.isAlphabetic(ch)) {
				if(ch == 'r' || ch == 'l') {
					ca[i] = 'w';
					wasE = false;
				}
				else if (ch == 'R' || ch == 'L') {
					ca[i] = 'W';
					wasE = false;
				}
				else if(ch == 'e') {
					if(wasE) {
						ca[i] = 'i';
						ca[i-1] = Character.isLowerCase(ca[i-1]) ? 'i' : 'I';
					}
					wasE = true;
				} 				
				else if(ch == 'E') {
					if(wasE) {
						ca[i] = 'I';
						ca[i-1] = Character.isLowerCase(ca[i-1]) ? 'i' : 'I';
					}
					wasE = true;
				} else {
					wasE = false;
				}
			}
		}
		return new String(ca);
	}

}