package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import GUI.GUI;

public class BroadcastListener extends BroadcastThread implements Runnable {
	private final BroadcastResponseHandler handler;
	private static final int BUFFER_SIZE = 20;

	public BroadcastListener(final DatagramSocket sendSocket, final DatagramPacket sendPacket, final BroadcastResponseHandler handler) {
		super(sendSocket, sendPacket);
		this.handler = handler;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		final byte[] data = new byte[BUFFER_SIZE];
		try {
			final DatagramSocket reciveSocket = new DatagramSocket(BROADCAST_PORT);
			final DatagramPacket recivePacket = new DatagramPacket(data, BUFFER_SIZE);

			for (;;) {
				reciveSocket.receive(recivePacket);

				if (!InetAddress.getLocalHost().equals(recivePacket.getAddress())) {

					// System.out.println("recive: " + Arrays.toString(data));
					if (data[0] == 1) { // I WAS FORCED!
						sendSocket.send(sendPacket);
					}

					recivePacket.setData(Arrays.copyOfRange(data, 1, data.length));
					handler.handle(recivePacket);
					Arrays.fill(data, (byte) 0);
					recivePacket.setData(data);

				}
			}
		} catch (IOException e) {
			GUI.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}
}
