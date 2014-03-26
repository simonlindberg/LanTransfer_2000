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
	final protected static String FORCE_BROADCAST_MSG = "FORCE_BROADCAST"; // Poblem om anv�ndaren heter just detta.
	protected static Set<User> users;
	protected static String ip;

	private static InetAddress getBroadcastAddress() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();

			if (!networkInterface.isLoopback()) {

				for (final InterfaceAddress interfaceAddr : networkInterface.getInterfaceAddresses()) {
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
			ip = InetAddress.getLocalHost().toString().split("/")[1];
		} catch (UnknownHostException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to fetch ip");
		}
		
		// TODO Auto-generated method stub
		new BroadcastListener(new BroadcastResponseHandler() {

			@Override
			public void handle(final DatagramPacket packet) {
				String data = new String(packet.getData(), 0, packet.getLength());
				switch (data) {
				case FORCE_BROADCAST_MSG:
					System.out.println("SOMEONE FORCED ME!");
					BroadcastSender.forceResponse();
					break;
				default:
					// remove first slash
					String otherIp = packet.getAddress().toString().substring(1);
					if (!ip.equals(otherIp)) {
						users.add(new User(data, otherIp, packet.getPort()));
						GUI.populateGUI(users);
						System.out.println(data + " -> " + otherIp + ":" + packet.getPort());
					}
				}
			}
		}).start();

		new BroadcastSender(System.getProperty("user.name")).start();
	}

}
