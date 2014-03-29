package chat;

import java.io.OutputStream;
import java.io.PrintWriter;

public class ChatSender {
	private final PrintWriter writer;

	public ChatSender(final OutputStream os) {
		writer = new PrintWriter(os);
	}

	public void send(final String msg) {
		writer.println(msg);
		writer.flush();
	}
}
