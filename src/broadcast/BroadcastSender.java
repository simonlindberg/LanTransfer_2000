package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import GUI.GUI;
import Protocol.Protocol;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private byte[] defaultMessage;
	private static DatagramSocket ds;
	private static DatagramPacket dp;

	public BroadcastSender(final String id) throws SocketException {
		BroadcastData[] defaultData = { new BroadcastData(Protocol.BROADCAST, id) };
		defaultMessage = Protocol.format(defaultData).getBytes();
		ds = new DatagramSocket();
		ds.setBroadcast(true); // Behövs defacto inte, men why not.
		ds.connect(Broadcast.BROADCAST_ADDR, Broadcast.BROADCAST_PORT);
		dp = new DatagramPacket(defaultMessage, defaultMessage.length,
				Broadcast.BROADCAST_ADDR, Broadcast.BROADCAST_PORT);
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
			String msg = new BroadcastData(Protocol.FORCE_BROADCAST, null)
					.toString();
			final DatagramPacket packet = new DatagramPacket(msg.getBytes(),
					msg.getBytes().length, Broadcast.BROADCAST_ADDR,
					Broadcast.BROADCAST_PORT);
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

	public static void sendTestFile() {
// does nothing atm
	}
}
