package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.User;
import GUI.ChatPanel;

public class ChatReciver extends Thread implements Runnable {

	private final BufferedReader br;
	private final User user;
	private final ChatPanel chatPanel;

	public ChatReciver(final InputStream inputStream, final ChatPanel chatPanel, final User user) {
		this.br = new BufferedReader(new InputStreamReader(inputStream));
		this.user = user;
		this.chatPanel = chatPanel;
	}

	@Override
	public void run() {
		try {
			String line = br.readLine();
			while (line != null) {
				chatPanel.showMessage(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			// Connection is dead, i.e. user is offline, let thread die.
			user.setOffline();
			System.out.println("Connection has died in ChatReceiver...");
		}
	}
}
