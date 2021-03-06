package tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import network.Initiator;
import network.NetworkUtils;
import network.fileTransfer.FileTransferIntermediary;
import network.fileTransfer.FileTransferPrompter;
import network.fileTransfer.FileTransferReciver;
import network.fileTransfer.FileTransferSender;

import org.junit.Test;

public class TestFileTransfer {

	private static final String save = "folder";
	private static final CountDownLatch globalLatch = new CountDownLatch(2);

	@Test
	public void test() throws IOException, InterruptedException {
		final byte[] senderCont = new byte[10_000];
		final Path saveFolder = Paths.get(save);
		final Path saveFile = Paths.get(save, "file");
		Files.createDirectory(saveFolder);
		final List<Path> senderfile = createRandomFile(senderCont);
		try {
			final AtomicBoolean reciveCancelled = new AtomicBoolean(false);
			final AtomicBoolean reciveFailed = new AtomicBoolean(false);
			final AtomicBoolean reciveDone = new AtomicBoolean(false);
			final AtomicInteger reciveVal = new AtomicInteger();

			final AtomicBoolean senderCancelled = new AtomicBoolean(false);
			final AtomicBoolean senderFailed = new AtomicBoolean(false);
			final AtomicBoolean senderDone = new AtomicBoolean(false);
			final AtomicInteger senderVal = new AtomicInteger();

			startServer(reciveCancelled, reciveFailed, reciveDone, reciveVal);

			Thread.sleep(10); // Låt trådarna starta i rätt ordning!

			startFileSender(senderCancelled, senderFailed, senderDone, senderVal, senderfile);

			globalLatch.await();

			assertFalse(reciveCancelled.get());
			assertFalse(senderCancelled.get());

			assertFalse(reciveFailed.get());
			assertFalse(senderFailed.get());

			assertTrue(reciveDone.get());
			assertTrue(senderDone.get());

			assertEquals(100, reciveVal.get());
			assertEquals(100, senderVal.get());

			assertArrayEquals(senderCont, Files.readAllBytes(saveFile));
		} finally {
			Files.delete(senderfile.get(0));
			try {
				Files.delete(saveFile);
			} catch (Exception e) {
			}
			Files.delete(saveFolder);
		}
	}

	private void startFileSender(final AtomicBoolean senderCancelled, final AtomicBoolean senderFailed, final AtomicBoolean senderDone,
			final AtomicInteger senderVal, final List<Path> files) throws IOException {
		new FileTransferSender(files, "127.0.0.1", new FileTransferIntermediary() {

			@Override
			public void setValue(int value) {
				senderVal.set(value);
			}

			@Override
			public void setString(String string) {
			}

			@Override
			public void fail(Exception e) {
				senderFailed.set(true);
				globalLatch.countDown();
			}

			@Override
			public void done() {
				senderDone.set(true);
				globalLatch.countDown();
			}

			@Override
			public void cancel() {
				senderCancelled.set(true);
				globalLatch.countDown();
			}
		}, new Socket()).start();
	}

	private void startServer(final AtomicBoolean cancelled, final AtomicBoolean failed, final AtomicBoolean done, final AtomicInteger val) {
		NetworkUtils.startFileTransferServer(new Initiator() {

			@Override
			public void init(final Socket socket) {
				new FileTransferReciver(socket, new FileTransferPrompter() {

					@Override
					public FileTransferIntermediary promptFileTransfer(List<String> fileNames, List<Long> fileSizes,
							AtomicReference<String> savePlace, CountDownLatch latch, Socket socket) {
						savePlace.set(save);
						final FileTransferIntermediary fileTransferIntermediary = new FileTransferIntermediary() {

							@Override
							public void setValue(int value) {
								val.set(value);
							}

							@Override
							public void setString(String string) {
							}

							@Override
							public void fail(Exception e) {
								failed.set(true);
								globalLatch.countDown();
							}

							@Override
							public void done() {
								done.set(true);
								globalLatch.countDown();
							}

							@Override
							public void cancel() {
								cancelled.set(true);
								globalLatch.countDown();
							}
						};
						latch.countDown();
						return fileTransferIntermediary;
					}
				}).start();
			}
		});
	}

	private static List<Path> createRandomFile(final byte[] senderCont) throws IOException {
		new Random().nextBytes(senderCont);
		final List<Path> files = new ArrayList<Path>();
		final Path path = Paths.get("file");
		Files.createFile(path);
		try (final OutputStream o = Files.newOutputStream(path)) {
			o.write(senderCont);
		}
		files.add(path);
		return files;
	}
}