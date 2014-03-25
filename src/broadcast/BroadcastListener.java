package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

public class BroadcastListener extends Thread implements Runnable {
	private final BroadcastResponseHandler handler;
	private static final int BUFFER_SIZE = 100;

	public BroadcastListener(final BroadcastResponseHandler handler) {
		this.handler = handler;
	}

	public void run() {
		byte[] inBuf = new byte[BUFFER_SIZE];
		MulticastSocket ms = null;
		try {
			ms = new MulticastSocket(Broadcast.BROADCAST_PORT);
//			ms.joinGroup(Broadcast.getBroadcastAddress());
			DatagramPacket packet;

			for (;;) {
				packet = new DatagramPacket(inBuf, inBuf.length);
				ms.receive(packet);
		        String msg = new String(inBuf, 0, packet.getLength());
		        System.out.println("From " + packet.getAddress() + " Msg : " + msg);
				handler.handle(packet);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ms != null) {
				ms.close();
			}
		}
	}

}
