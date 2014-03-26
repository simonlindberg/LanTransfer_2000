package broadcast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public abstract class BroadcastThread extends Thread {

	protected final static int BROADCAST_PORT = 31173;
	private static InetAddress BROADCAST_ADDR;

	// protected message;
	protected final DatagramSocket sendSocket;
	protected final DatagramPacket sendPacket;

	public BroadcastThread(final String id) throws SocketException {
		final byte[] idBytes = id.getBytes();

		final byte[] message = new byte[idBytes.length + 1];
		message[0] = 0; // NOT FORCED!
		System.arraycopy(idBytes, 0, message, 1, idBytes.length);

		sendSocket = new DatagramSocket();
		sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
		sendSocket.connect(getBroadcastAddress(), BROADCAST_PORT);

		sendPacket = new DatagramPacket(message, message.length);
	}

	protected static InetAddress getBroadcastAddress() throws SocketException {
		if (BROADCAST_ADDR == null) {
			BROADCAST_ADDR = actuallyGetBroadcastAddress();
		}
		return BROADCAST_ADDR;
	}

	private static InetAddress actuallyGetBroadcastAddress() throws SocketException {
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
}
