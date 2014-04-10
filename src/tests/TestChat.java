package tests;

import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import chat.ChatInitiator;
import chat.ChatReciverThread;
import chat.ChatSender;
import chat.ChatServerThread;
import chat.MessageReciver;

public class TestChat {

	@Test
	public void test() throws IOException, InterruptedException {
		final String correctMessage = "this is a message!!\n\nThis is still the same message!!";

		new ChatServerThread(new ChatInitiator() {

			@Override
			public void initChat(final Socket socket) throws IOException {
				new ChatReciverThread(socket.getInputStream(), new MessageReciver() {

					@Override
					public void newMessage(final String msg) {
						assertEquals(correctMessage, msg);
					}
				}).start();
			}
		}).start();
		Thread.sleep(200);
		final Socket socket = new Socket("127.0.0.1", ChatServerThread.CHAT_PORT);
		final ChatSender chatSender = new ChatSender(socket.getOutputStream());
		chatSender.send(correctMessage);
		chatSender.send(correctMessage);
		chatSender.send(correctMessage);
		chatSender.send(correctMessage);

		Thread.sleep(200);
		socket.close();
	}

}
