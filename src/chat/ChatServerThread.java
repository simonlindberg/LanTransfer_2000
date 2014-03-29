package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import main.User;

public class ChatServerThread extends Thread implements Runnable {

	public static final int CHAT_PORT = 8888;
	private final Map<String, User> users;

	public ChatServerThread(Map<String, User> users) {
		this.users = users;
	}

	@Override
	public void run() {
		ServerSocket chatServer = null;
		try {
			chatServer = new ServerSocket(CHAT_PORT);
			for (;;) {
				Socket socket = chatServer.accept();
				synchronized (users) {
					final User user = users.get(socket.getInetAddress().getHostAddress());
					user.startChat(socket);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
