package broadcast;

public class Protocol {

	final protected static String FORCE_BROADCAST_MSG = "FORCE_BROADCAST";
	final protected static String BROADCAST = "BROADCAST";
	final protected static String DELIMITER = ";";
	
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
