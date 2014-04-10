package chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ChatSender {
	private final DataOutputStream out;

	public ChatSender(final OutputStream os) {
		out = new DataOutputStream(os);
	}

	public void send(final String msg) throws IOException {
		out.writeUTF(msg);
		out.flush();
	}
}
