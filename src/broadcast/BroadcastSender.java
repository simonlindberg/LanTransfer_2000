package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private final DatagramPacket packet; 

	public BroadcastSender(final String id) {
		final byte[] bytes = id.getBytes();
		packet = new DatagramPacket(bytes, bytes.length);
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			final DatagramSocket ds = new DatagramSocket();
			ds.setBroadcast(true); // Behövs defacto inte, men why not.
			ds.connect(Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);

			for (;;) {
				ds.send(packet);
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
