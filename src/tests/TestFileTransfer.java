package tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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

import org.junit.Test;

import fileTransfer.FileTransferInitiator;
import fileTransfer.FileTransferIntermediary;
import fileTransfer.FileTransferPrompter;
import fileTransfer.FileTransferReciver;
import fileTransfer.FileTransferSender;
import fileTransfer.FileTransferServer;

public class TestFileTransfer {

	private static final String save = "folder";

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
			Thread.sleep(20);
			startFileSender(senderCancelled, senderFailed, senderDone, senderVal, senderfile);

			Thread.sleep(300);

			assertTrue(!reciveCancelled.get());
			assertTrue(!senderCancelled.get());

			assertTrue(!reciveFailed.get());
			assertTrue(!senderFailed.get());

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
			}

			@Override
			public void done() {
				senderDone.set(true);
			}

			@Override
			public void cancel() {
				senderCancelled.set(true);
			}
		}, new Socket()).start();
	}

	private void startServer(final AtomicBoolean cancelled, final AtomicBoolean failed, final AtomicBoolean done, final AtomicInteger val) {
		new FileTransferServer(new FileTransferInitiator() {

			@Override
			public void initFileTransfer(Socket socket) {
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
							}

							@Override
							public void done() {
								done.set(true);
							}

							@Override
							public void cancel() {
								cancelled.set(true);
							}
						};
						latch.countDown();
						return fileTransferIntermediary;
					}
				}).start();
			}
		}).start();
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