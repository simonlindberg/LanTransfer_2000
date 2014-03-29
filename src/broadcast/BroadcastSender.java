package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastSender extends BroadcastThread implements Runnable {
	private static final long SEND_INTERVAL = 1000;

	public BroadcastSender(DatagramSocket sendSocket, DatagramPacket sendPacket) {
		super(sendSocket, sendPacket);
	}

	@Override
	public void run() {
		try {
			for (;;) {
				sendSocket.send(sendPacket);
				Thread.sleep(SEND_INTERVAL);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

}
