package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import GUI.GUI;

public class BroadcastListener extends BroadcastThread implements Runnable {
	private final BroadcastResponseHandler handler;
	private static final int BUFFER_SIZE = 100;

	public BroadcastListener(final String id, final BroadcastResponseHandler handler) throws SocketException {
		super(id);
		this.handler = handler;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		byte[] inBuf = new byte[BUFFER_SIZE];
		try {
			final DatagramSocket ds = new DatagramSocket(BROADCAST_PORT);
			final DatagramPacket packet = new DatagramPacket(inBuf, BUFFER_SIZE);

			for (;;) {
				ds.receive(packet);

				if (!InetAddress.getLocalHost().equals(packet.getAddress())) {

					final byte[] data = packet.getData();

					if (data[0] == 1) { // FORCED
						sendSocket.send(sendPacket);
					}

					packet.setData(data, 1, data.length - 1);
					handler.handle(packet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to receive data");
		}
	}
}
