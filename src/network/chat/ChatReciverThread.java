package network.chat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ChatReciverThread extends Thread implements Runnable {

	private final DataInputStream in;
	private final MessageReciver user;

	public ChatReciverThread(final InputStream inputStream, final MessageReciver user) {
		this.in = new DataInputStream(inputStream);
		this.user = user;
	}

	@Override
	public void run() {
		try {
			String msg = in.readUTF();
			while (msg != null) {
				user.newMessage(msg);
				msg = in.readUTF();
			}
		} catch (IOException e) {
			// Connection is dead, User might be offline. Connection might just dropped.
			System.out.println("Connection has died in ChatReceiver...");
		}
	}
}
