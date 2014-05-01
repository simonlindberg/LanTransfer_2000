package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import network.Initiator;
import network.NetworkUtils;
import network.chat.ChatReciverThread;
import network.chat.ChatSender;
import network.chat.MessageReciver;

import org.junit.Test;

public class TestChat {

	@Test
	public void test() throws IOException, InterruptedException {
		final String correctMessage = "this is a message!!\n\nThis is still the same message!!";
		final AtomicReference<String> message = new AtomicReference<String>();
		final AtomicBoolean recived = new AtomicBoolean();
		final AtomicBoolean seen = new AtomicBoolean();

		final CountDownLatch messageLatch = new CountDownLatch(1);
		final CountDownLatch recivedLatch = new CountDownLatch(1);
		final CountDownLatch seenLatch = new CountDownLatch(1);

		NetworkUtils.startChatServer(new Initiator() {

			@Override
			public void init(final Socket socket) throws IOException {
				new ChatReciverThread(socket.getInputStream(), socket.getOutputStream(), new MessageReciver() {

					@Override
					public void newMessage(final String msg, final int id) {
						message.set(msg);
						messageLatch.countDown();
					}

					@Override
					public void sentMessage(int id) {
						recived.set(true);
						recivedLatch.countDown();
					}

					@Override
					public void seenMessage(int id) {
						seen.set(true);
						seenLatch.countDown();
					}
				}).start();
			}
		});

		Thread.sleep(10); // Låt tråden starta i rätt ordning.

		try (final Socket socket = new Socket("127.0.0.1", NetworkUtils.CHAT_PORT)) {
			final ChatSender chatSender = new ChatSender(socket.getOutputStream());
			final int id = chatSender.sendMessage(correctMessage);

			messageLatch.await();
			assertEquals(correctMessage, message.get());
			System.out.println(message.get());

			chatSender.sendSeenConfirm(id);
			seenLatch.await();
			assertTrue(seen.get());
		}

	}

}
