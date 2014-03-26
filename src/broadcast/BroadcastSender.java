package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import GUI.GUI;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private static DatagramSocket ds;
	private static DatagramPacket dp;
	private byte[] defaultMessage;
	private static byte[] forcedMessage;

	public BroadcastSender(final String id) throws SocketException {
		byte[] idBytes = id.getBytes();
		defaultMessage = new byte[idBytes.length + 1];
		forcedMessage = new byte[idBytes.length + 1];
		defaultMessage[0] = 0;
		forcedMessage[1] = 1;
		for (int i = 0; i < idBytes.length; i++) {
			defaultMessage[i + 1] = idBytes[i];
			forcedMessage[i + 1] = idBytes[i];
		}
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
//				Broadcast.resetUserlist();
//				ds.send(dp);
				// Always force...?
				forceBroadcast();
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			GUI.showError("Fatal error", "Unable to send data");
		}
	}

	public static void forceBroadcast() {
		try {
			Broadcast.resetUserlist();
			final DatagramPacket packet = new DatagramPacket(forcedMessage,
					forcedMessage.length, Broadcast.BROADCAST_ADDR,
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
			GUI.showError("Fatal error", "Unable to send data");
		}
	}

}
