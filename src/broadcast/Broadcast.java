package broadcast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Broadcast {

	public static InetAddress getBroadcastAddress() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();
			

		}
	}
}
