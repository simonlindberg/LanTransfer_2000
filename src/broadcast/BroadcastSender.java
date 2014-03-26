package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import GUI.GUI;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private byte[] bytes;
	private static DatagramSocket ds;
	private static DatagramPacket dp;

	public BroadcastSender(final String id) throws SocketException {
		BroadcastData[] defaultData = { new BroadcastData(Protocol.BROADCAST, id) };
		bytes = Protocol.format(defaultData).getBytes();
		ds = new DatagramSocket();
		ds.setBroadcast(true); // Behövs defacto inte, men why not.
		ds.connect(Broadcast.BROADCAST_ADDR, Broadcast.BROADCAST_PORT);
		dp = new DatagramPacket(bytes, bytes.length, Broadcast.BROADCAST_ADDR,
				Broadcast.BROADCAST_PORT);
	}

	@Override
	public void run() {
		try {
			for (;;) {
				ds.send(dp);
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to send data");
		}
	}

	public static void forceBroadcast() {
		try {
			final DatagramPacket packet = new DatagramPacket(
					Protocol.FORCE_BROADCAST_MSG.getBytes(),
					Protocol.FORCE_BROADCAST_MSG.getBytes().length,
					Broadcast.BROADCAST_ADDR, Broadcast.BROADCAST_PORT);
			ds.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to send data");
		}
	}

	public static void forceResponse() {
		try {
			ds.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
