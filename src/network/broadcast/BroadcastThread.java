package network.broadcast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public abstract class BroadcastThread extends Thread {
	public static final int NORMAL = 0;
	public static final int FORCED = 1;
	public static final int GOING_OFFLINE = 2;

	private static InetAddress BROADCAST_ADDR;

	// protected message;
	protected final DatagramSocket sendSocket;
	protected final DatagramPacket sendPacket;

	public BroadcastThread(final DatagramSocket sendSocket, final DatagramPacket sendPacket) {
		this.sendPacket = sendPacket;
		this.sendSocket = sendSocket;
	}

	public static InetAddress getBroadcastAddress() throws SocketException {
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
