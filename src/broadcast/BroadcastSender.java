package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import GUI.GUI;

public class BroadcastSender extends BroadcastThread implements Runnable {
	private static final long SEND_INTERVAL = 10000;

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
			GUI.showError("Fatal error", "Unable to send data");
		}
	}

}
