package broadcast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import GUI.GUI;

public class Broadcast {

	final protected static int BROADCAST_PORT = 6666;
	protected static InetAddress BROADCAST_ADDR;
	protected static Set<User> users;
	protected static String ownIp;

	private static InetAddress getBroadcastAddress() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
				.getNetworkInterfaces();

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces
					.nextElement();

			if (!networkInterface.isLoopback()) {

				for (final InterfaceAddress interfaceAddr : networkInterface
						.getInterfaceAddresses()) {
					final InetAddress address = interfaceAddr.getBroadcast();

					if (address != null) {
						return address;
					}
				}
			}
		}
		throw new SocketException("No broadcast address found!");
	}

	public static void start() throws SocketException {
		BROADCAST_ADDR = getBroadcastAddress();
		users = new HashSet<User>();

		try {
			ownIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to fetch ip");
		}

		new BroadcastListener(new BroadcastResponseHandler() {

			@Override
			public void handle(final DatagramPacket packet) {
				// Payload
				byte[] raw = packet.getData();
				String data = new String(raw, 1, packet.getLength() - 1);
				System.out.println("RECEIVE RAW: " + data);
				String otherIp = packet.getAddress().getHostAddress();
				if (!ownIp.equals(otherIp)) {
					resetUserlist();
					users.add(new User(data, otherIp, packet.getPort()));
					GUI.populateGUI(users);
					System.out.println("RECEIVE: " + data + " -> " + otherIp
							+ ":" + packet.getPort());
				}
			}
		}).start();

		new BroadcastSender(System.getProperty("user.name")).start();
	}

	private static void resetUserlist() {
		GUI.clearGUI();
		users.clear();
	}

}
