package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastListener extends Thread implements Runnable {
	private final BroadcastResponseHandler handler;
	private static final int BUFFER_SIZE = 100;

	public BroadcastListener(final BroadcastResponseHandler handler) {
		this.handler = handler;
	}

	public void run() {
		byte[] smallBuffer = new byte[BUFFER_SIZE];
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(Broadcast.BROADCAST_PORT, Broadcast.getBroadcastAddress());
			final DatagramPacket packet = new DatagramPacket(smallBuffer, smallBuffer.length);
			for (;;) {
				ds.receive(packet);
				handler.handle(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (ds != null) {
				ds.close();
			}
		}
	}

}
