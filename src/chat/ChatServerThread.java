package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerThread extends Thread implements Runnable {

	private int CHAT_PORT = 8888;
	private ChatHandler ch;
	
	public ChatServerThread(ChatHandler ch) {
		this.ch = ch;
	}

	@Override
	public void run() {
		ServerSocket chatServer = null;
		try {
			chatServer = new ServerSocket(CHAT_PORT);
			for (;;) {
				Socket s = chatServer.accept();
				ch.handleInit(s);
				new ChatThread(ch, s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
