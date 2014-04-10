package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import user.User;

public class ChatReciver extends Thread implements Runnable {

	private final BufferedReader br;
	private final User user;

	public ChatReciver(final InputStream inputStream, final User user) {
		this.br = new BufferedReader(new InputStreamReader(inputStream));
		this.user = user;
	}

	@Override
	public void run() {
		try {
			String line = br.readLine();
			while (line != null) {
				user.newMessage(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			// Connection is dead, User might be offline. Connection might just dropped.
			System.out.println("Connection has died in ChatReceiver...");
		}
	}
}
