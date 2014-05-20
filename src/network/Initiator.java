package network;

import java.io.IOException;
import java.net.Socket;

public interface Initiator {
	public void init(final Socket socket) throws IOException;
}
