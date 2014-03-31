package chat;

import java.io.IOException;
import java.net.Socket;

public interface ChatHandler {

	void initChat(Socket socket) throws IOException;
}
