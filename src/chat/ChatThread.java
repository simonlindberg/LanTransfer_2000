package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChatThread extends Thread implements Runnable {

	private final BufferedReader br;

	public ChatThread(final InputStream inputStream) {
		this.br = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public void run() {
		try {
			for (;;) {
				String msg = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
