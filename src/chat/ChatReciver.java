package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import GUI.ChatPanel;

public class ChatReciver extends Thread implements Runnable {

	private final BufferedReader br;
	private final ChatPanel chatPanel;

	public ChatReciver(final InputStream inputStream, final ChatPanel chatPanel) {
		this.br = new BufferedReader(new InputStreamReader(inputStream));
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
			e.printStackTrace();
		}
	}
}
