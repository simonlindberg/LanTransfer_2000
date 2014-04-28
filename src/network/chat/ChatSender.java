package network.chat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChatSender {
	private final DataOutputStream out;
	private int id = 0;

	public ChatSender(final OutputStream os) {
		out = new DataOutputStream(os);
	}

	public int sendMessage(final String msg) throws IOException {
		id++;
		out.writeUTF(id + "." + msg);
		out.flush();
		return id;
	}

	public void sendSeenConfirm(final int id) throws IOException {
		out.writeUTF("s" + id);
	}
}
