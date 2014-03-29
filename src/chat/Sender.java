package chat;

import java.io.OutputStream;
import java.io.PrintWriter;

public class Sender {
	private final PrintWriter writer;

	public Sender(final OutputStream os) {
		writer = new PrintWriter(os);
	}

	public void send(final String msg) {
		writer.println(msg);
	}
}
