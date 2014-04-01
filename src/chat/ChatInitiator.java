package chat;

import java.io.IOException;
import java.net.Socket;

public interface ChatInitiator {

	public void initChat(Socket socket) throws IOException;
}
