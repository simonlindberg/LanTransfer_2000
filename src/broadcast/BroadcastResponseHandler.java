package broadcast;

import java.net.DatagramPacket;

public interface BroadcastResponseHandler {
	public void handle(final DatagramPacket packet);
}
