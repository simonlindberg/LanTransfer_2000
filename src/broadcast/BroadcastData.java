package broadcast;

import java.util.ArrayList;
import java.util.List;

public class BroadcastData {

	private String protocol;
	private String value;

	public BroadcastData(String protocol, String value) {
		this.protocol = protocol;
		this.value = value;
	}

	public String toString() {
		return protocol + "=" + value;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getValue() {
		return value;
	}

	public static BroadcastData[] parse(String[] data) {
		List<BroadcastData> bd = new ArrayList<BroadcastData>();
		for (String s : data) {
			if (s.matches("^[^;=]+=[^;=]+$")) {
				String[] partData = s.split("=");
				bd.add(new BroadcastData(partData[0], partData[1]));
//			} else if (s.matches("^[^;=]+$")) { // required for force_broadcast which has no value
//				bd.add(new BroadcastData(s, null));
			}
			else {
				throw new RuntimeException("Broadcast data bad format" + s);
			}
		}

		return bd.toArray(new BroadcastData[bd.size()]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BroadcastData other = (BroadcastData) obj;
		if (protocol == null) {
			if (other.protocol != null)
				return false;
		} else if (!protocol.equals(other.protocol))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
