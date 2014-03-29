package chat;

import java.net.Socket;

public interface ChatHandler {
	public void handleInit(Socket s);
	public void handleChat();
}
