package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import GUI.Gui;

public class BroadcastSender extends BroadcastThread implements Runnable {
	private static final long SEND_INTERVAL = 1000;
	private final byte[] message;

	public BroadcastSender(final DatagramSocket sendSocket, final DatagramPacket sendPacket, final byte[] message) {
		super(sendSocket, sendPacket);
		this.message = message;
	}

	@Override
	public void run() {
		try {
			message[0] = FORCED;
			sendSocket.send(sendPacket);
			message[0] = NORMAL;
			for (;;) {
				Thread.sleep(SEND_INTERVAL);
				sendSocket.send(sendPacket);
			}
		} catch (InterruptedException | IOException e) {
			Gui.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}

}
