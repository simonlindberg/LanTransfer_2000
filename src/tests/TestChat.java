package tests;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
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
		final CountDownLatch latch = new CountDownLatch(1);
		new ChatServerThread(new ChatInitiator() {

			@Override
			public void initChat(final Socket socket) throws IOException {
				new ChatReciverThread(socket.getInputStream(), new MessageReciver() {

					@Override
					public void newMessage(final String msg) {
						recived.set(msg);
						latch.countDown();
					}
				}).start();
			}
		}).start();

		Thread.sleep(1); // Låt tråden starta i rätt ordning.

		try (final Socket socket = new Socket("127.0.0.1", ChatServerThread.CHAT_PORT)) {
			new ChatSender(socket.getOutputStream()).send(correctMessage);
		}
		latch.await();

		assertEquals(correctMessage, recived.get());
	}

}
