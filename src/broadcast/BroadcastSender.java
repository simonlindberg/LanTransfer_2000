package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private static DatagramPacket packet;
	private byte[] bytes;
	private static DatagramSocket ds;

	public BroadcastSender(final String id) throws SocketException {
		bytes = id.getBytes();
		ds = new DatagramSocket();
		ds.setBroadcast(true); // Behövs defacto inte, men why not.
		ds.connect(Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);
			
			for (;;) {
				ds.send(packet);
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void forceBroadcast() {
		try {
			String msg = "FORCE_BROADCAST";
			DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, Broadcast.getBroadcastAddress(), Broadcast.BROADCAST_PORT);
			
			ds.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
