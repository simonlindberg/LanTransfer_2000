package broadcast;

import java.net.DatagramPacket;

public interface BroadcastResponseHandler {
	public void handleBroadcast(final DatagramPacket packet);

	public void handleOfflineMessage(final DatagramPacket packet);
}
