package chat;

import java.io.IOException;
import java.net.ServerSocket;

import GUI.GUI;

public class ChatServerThread extends Thread implements Runnable {

	public static final int CHAT_PORT = 8888;
	private final ChatHandler chatHandler;

	public ChatServerThread(ChatHandler chatHandler) {
		this.chatHandler = chatHandler;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			final ServerSocket chatServer = new ServerSocket(CHAT_PORT);
			for (;;) {
				chatHandler.initChat(chatServer.accept());
			}
		} catch (IOException e) {
			GUI.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}
}
