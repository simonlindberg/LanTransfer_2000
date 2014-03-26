package Protocol;

import broadcast.BroadcastData;

public class Protocol {

	final public static String FORCE_BROADCAST = "FORCE_BROADCAST";
	final public static String BROADCAST = "BROADCAST";
	final public static String DELIMITER = ";";
	
	public static String format(BroadcastData[] data) {
		StringBuilder result = new StringBuilder();
		for (BroadcastData d : data) {
			result.append(d.toString());
			result.append(DELIMITER);
		}
		// remove last ;
		return result.toString().substring(0, result.toString().length() - 1);
	}

}
