package radium.common;

import java.util.Map;

final public class Substitutor {

	private Substitutor() {
		super();
	}
	
	public static String substitute(String text, Map<String, String> variables) {
		String substitutedText = text;
		for (Map.Entry<String, String> variable : variables.entrySet()) {
			substitutedText = substitutedText.replace("{" + variable.getKey() + "}", variable.getValue());
		}
		return substitutedText;
	}
	
}
