package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import GUI.GUI;

public class BroadcastListener extends Thread implements Runnable {
	private final BroadcastResponseHandler handler;
	private static final int BUFFER_SIZE = 100;

	public BroadcastListener(final BroadcastResponseHandler handler) {
		this.handler = handler;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		byte[] inBuf = new byte[BUFFER_SIZE];
		try {
			final DatagramSocket ds = new DatagramSocket(Broadcast.BROADCAST_PORT);
			final DatagramPacket packet = new DatagramPacket(inBuf, BUFFER_SIZE);

			for (;;) {
				Broadcast.resetUserlist();
				ds.receive(packet);
				if (packet.getData()[0] == 1) { // FORCED
					BroadcastSender.forceResponse();
				}
				handler.handle(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to receive data");
		}
	}
}
