package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 1000;
	private final DatagramPacket packet; 
	
	public BroadcastSender(final String id) throws SocketException {
		final byte[] bytes = id.getBytes();
		packet = new DatagramPacket(bytes, bytes.length, Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);
	}
	
	@Override
	public void run() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
			ds.setBroadcast(true); // Behövs defacto inte, men why not.
			ds.connect(Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);
			
			for (;;) {
				ds.send(packet);
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (ds != null) {
				ds.close();
			}
		}
	}
}
