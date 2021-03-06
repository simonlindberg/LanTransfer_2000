package network.fileTransfer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import main.Utils;
import network.NetworkUtils;

public class FileTransferReciver extends Thread implements Runnable {

	private final Socket socket;
	private final FileTransferPrompter ftp;

	public FileTransferReciver(final Socket socket, final FileTransferPrompter ftp) {
		this.socket = socket;
		this.ftp = ftp;
	}

	@Override
	public void run() {
		/*
		 * 1. Ta emot antal
		 * 2. För alla filer: ta emot filnamn och storlek.
		 * 3. Fråga användaren JA/NEJ
		 * 4a. (NEJ) Skicka NEJ (0)
		 * 4b. (JA) Skicka OKEY (1)
		 * 5. För alla filer: ta emot filnamn, storlek OCH data.
		 */
		FileTransferIntermediary intermediary = null;
		Path currentFile = null;
		OutputStream fos = null;

		try {
			final DataInputStream input = new DataInputStream(socket.getInputStream());

			final long numberOfTopLevelFiles = input.readLong();

			final List<String> fileNames = new ArrayList<>();
			final List<Long> fileSizes = new ArrayList<>();

			for (int i = 0; i < numberOfTopLevelFiles; i++) {
				fileNames.add(input.readUTF());// Filename
				fileSizes.add(input.readLong()); // File size
			}

			final long totalSize = sum(fileSizes);

			// Prompt user...
			final CountDownLatch latch = new CountDownLatch(1);
			final AtomicReference<String> savePlace = new AtomicReference<>(null);

			intermediary = ftp.promptFileTransfer(fileNames, fileSizes, savePlace, latch, socket);

			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (!interrupted()) {
							socket.getOutputStream().write(NetworkUtils.NOTHING);
							Thread.sleep(100);
						}
					} catch (Exception e) {
						latch.countDown();
					}
				}
			});

			thread.start();

			latch.await(); // Wait for user interaction!

			thread.interrupt();

			final String folder = savePlace.get();

			// User cancelled.
			if (folder == null) {
				socket.getOutputStream().write(NetworkUtils.CANCEL);
				intermediary.cancel();
				return;
			}

			// Send OKEY!
			socket.getOutputStream().write(NetworkUtils.ACCEPT);

			// Total amount recived.
			long totalRecived = 0;
			final long start = System.currentTimeMillis();
			for (;;) {
				final String filename = input.readUTF();
				final long size = input.readLong();

				final byte[] buffer = new byte[32768];

				currentFile = Paths.get(folder, filename);

				Utils.createParentFolders(currentFile);

				fos = Files.newOutputStream(currentFile);

				long read = 0;
				while (read != size) {
					final int toRead = (int) ((size - read) > buffer.length ? buffer.length : size - read);
					final int n = input.read(buffer, 0, toRead);
					fos.write(buffer, 0, n);
					read = read + n;
					totalRecived += n;

					final int percentage = (int) (100 * (totalRecived / (double) totalSize));
					final long bytesPerMs = totalRecived / (System.currentTimeMillis() - start) * 1000;

					intermediary.setValue(percentage);
					intermediary.setString(Utils.shorten(filename) + "  " + Utils.readbleTransferSpeed(bytesPerMs));
				}
				fos.close();

				if (totalRecived == totalSize) {
					intermediary.done();
					return;
				}
			}

		} catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
			if (intermediary != null) {
				intermediary.fail(e);
			}

			if (currentFile != null) {
				try {
					Files.delete(currentFile);
				} catch (IOException e1) {
				}
			}
			e.printStackTrace();
		} finally {
			try {
				socket.close();
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
			}
		}

	}

	private long sum(final List<Long> fileSizes) {
		long sum = 0;
		for (final long s : fileSizes) {
			sum += s;
		}
		return sum;
	}
}
