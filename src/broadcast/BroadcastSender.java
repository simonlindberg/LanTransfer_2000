package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class BroadcastSender extends Thread implements Runnable {
	private static final long SEND_INTERVAL = 10000;
	private static DatagramSocket ds;
	private static DatagramPacket responsePacket;
	private static DatagramPacket requestPacket;
	private byte[] responseMessage;
	private static byte[] requestMessage;

	public BroadcastSender(final String id) throws SocketException {
		byte[] idBytes = id.getBytes();
		responseMessage = new byte[idBytes.length + 1];
		requestMessage = new byte[idBytes.length + 1];
		responseMessage[0] = 0;
		requestMessage[0] = 1;
		for (int i = 0; i < idBytes.length; i++) {
			responseMessage[i + 1] = idBytes[i];
			requestMessage[i + 1] = idBytes[i];
		}
		ds = new DatagramSocket();
		ds.setBroadcast(true); // Behövs defacto inte, men why not.
		ds.connect(Broadcast.BROADCAST_ADDR, Broadcast.BROADCAST_PORT);

		responsePacket = new DatagramPacket(responseMessage,
				responseMessage.length, Broadcast.BROADCAST_ADDR,
				Broadcast.BROADCAST_PORT);
		requestPacket = new DatagramPacket(requestMessage,
				requestMessage.length, Broadcast.BROADCAST_ADDR,
				Broadcast.BROADCAST_PORT);
	}

	@Override
	public void run() {
		try {
			for (;;) {
				forceBroadcast();
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void forceBroadcast() {
		try {
			Broadcast.resetUserlist();
			ds.send(requestPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void forceResponse() {
		try {
			ds.send(responsePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
