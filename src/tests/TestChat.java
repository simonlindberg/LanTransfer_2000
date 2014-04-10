package tests;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

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
		final AtomicReference<String> recived = new AtomicReference<String>();
		new ChatServerThread(new ChatInitiator() {

			@Override
			public void initChat(final Socket socket) throws IOException {
				new ChatReciverThread(socket.getInputStream(), new MessageReciver() {

					@Override
					public void newMessage(final String msg) {
						recived.set(msg);
					}
				}).start();
			}
		}).start();
		Thread.sleep(50);
		final Socket socket = new Socket("127.0.0.1", ChatServerThread.CHAT_PORT);
		final ChatSender chatSender = new ChatSender(socket.getOutputStream());
		chatSender.send(correctMessage);

		Thread.sleep(100);
		socket.close();
		assertEquals(correctMessage, recived.get());
	}

}
