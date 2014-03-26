package broadcast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;

public class Broadcast {

	final static int BROADCAST_PORT = 6666;

	public static InetAddress getBroadcastAddress() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();

			if (!networkInterface.isLoopback()) {

				for (final InterfaceAddress interfaceAddr : networkInterface.getInterfaceAddresses()) {
					final InetAddress address = interfaceAddr.getBroadcast();

					if (address != null) {
						System.out.println(address);
						return address;
					}
				}
			}
		}
		throw new SocketException("No broadcast address found!");
	}

	public static void start() throws SocketException {
		// TODO Auto-generated method stub
		new BroadcastListener(new BroadcastResponseHandler() {

			@Override
			public void handle(final DatagramPacket packet) {
				System.out.println(new String(packet.getData()) + " -> " + packet.getAddress() + ":" + packet.getPort());
			}
		}).start();

		new BroadcastSender(new Random().nextInt() + "").start();
	}

}
