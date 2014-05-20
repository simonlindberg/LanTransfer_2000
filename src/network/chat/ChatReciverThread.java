package network.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChatReciverThread extends Thread implements Runnable {

	private final DataInputStream in;
	private final DataOutputStream out;
	private final MessageReciver reciver;

	public ChatReciverThread(final InputStream inputStream, final OutputStream outputStream, final MessageReciver reciver) {
		this.in = new DataInputStream(inputStream);
		this.out = new DataOutputStream(outputStream);
		this.reciver = reciver;
	}

	public static final int NEW_MSG = 0;
	public static final int SENT_MSG = 1;
	public static final int SEEN_MSG = 2;

	@Override
	public void run() {
		try {
			// TYPE+ID+MSG? - new msg
			for (;;) {
				final int type = in.read();
				final int id = in.readInt();

				if (type == NEW_MSG) {
					final String msg = in.readUTF();

					out.write(SENT_MSG);
					out.writeInt(id);

					reciver.newMessage(msg, id);
				} else if (type == SENT_MSG) {
					reciver.sentMessage(id);
				} else if (type == SEEN_MSG) {
					reciver.seenMessage(id);
				}
			}
		} catch (IOException e) {
			// Connection is dead, User might be offline. Connection might just dropped.
			System.out.println("Connection has died in ChatReceiver...");
		}
	}
}
