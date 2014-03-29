package chat;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ChatThread extends Thread implements Runnable {

	private Socket chatClient;
	private ChatHandler ch;

	public ChatThread(ChatHandler ch, Socket chatClient) {
		this.chatClient = chatClient;
		this.ch = ch;
	}

	@Override
	public void run() {
		try {
			InputStream in = chatClient.getInputStream();
			for (;;) {
				int msg = in.read();
				// ch to be used later
				System.out.println(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
