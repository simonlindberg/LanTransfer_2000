package chat;

import java.io.IOException;
import java.io.InputStream;

public class ChatThread extends Thread implements Runnable {

	private final InputStream inputStream;

	public ChatThread(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		final StringBuilder msg = new StringBuilder();
		try {
			for (;;) {
				int data = inputStream.read();
				while (data != -1) {
					data = inputStream.read();
					msg.append((char) data);
				}
				// ch to be used later
				System.out.println(msg);
				msg.setLength(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
