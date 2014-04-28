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

	private final String reciveString = "r";
	private final String seenString = "s";

	@Override
	public void run() {
		try {
			// id+ ".msg" - new msg
			// "r" + id" - recived msg id
			// "s" + id" - seen msg id
			for (;;) {
				final String msg = in.readUTF();
				if (msg.startsWith(reciveString)) {
					reciver.recivedMessage(Integer.parseInt(msg.substring(1)));
				} else {
					if (msg.startsWith(seenString)) {
						reciver.seenMessage(Integer.parseInt(msg.substring(1)));
					} else {
						final String[] split = msg.split("\\.", 2);
						out.writeUTF(reciveString + split[0]);
						reciver.newMessage(split[1], Integer.parseInt(split[0]));
					}
				}
			}
		} catch (IOException e) {
			// Connection is dead, User might be offline. Connection might just
			// dropped.
			System.out.println("Connection has died in ChatReceiver...");
		}
	}
}
