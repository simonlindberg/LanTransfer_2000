package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import GUI.GUI;

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
			GUI.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}

}
