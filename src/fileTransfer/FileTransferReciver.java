package fileTransfer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import user.User;
import GUI.Intermediary;

public class FileTransferReciver extends Thread implements Runnable {

	public static final int CANCEL = 0;
	public static final int ACCEPT = 1;

	private final Socket socket;
	private final User user;

	public FileTransferReciver(final Socket socket, final User user) {
		this.socket = socket;
		this.user = user;
	}

	@Override
	public void run() {
		/*
		 * 1. Ta emot antal 2. För alla filer: ta emot filnamn och storlek. 3.
		 * Fråga användaren JA/NEJ 4a. (NEJ) Skicka NEJ (0). 4b. (JA) Skicka
		 * OKEY (1). 5. För alla filer: ta emot filnamn, storlek OCH data.
		 */
		Intermediary intermediary = null;
		File currentFile = null;
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
			final AtomicReference<File> savePlace = new AtomicReference<>(null);

			intermediary = user.promptFileTransfer(fileNames, fileSizes, savePlace, latch, socket);

			latch.await(); // Wait for user interaction!

			final File folder = savePlace.get();

			// User cancelled.
			if (folder == null) {
				socket.getOutputStream().write(CANCEL);
				intermediary.cancel();
				return;
			}

			// Send OKEY!
			socket.getOutputStream().write(ACCEPT);

			// Total amount recived.
			long totalRecived = 0;
			final long start = System.currentTimeMillis();
			for (;;) {
				final String filename = input.readUTF();
				final long size = input.readLong();

				final byte[] buffer = new byte[32768];

				currentFile = new File(folder, filename);

				FileUtils.createParentFolders(currentFile);

				fos = new FileOutputStream(currentFile);

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
					intermediary.setString(filename + "  " + FileUtils.readbleTransferSpeed(bytesPerMs));
				}
				fos.close();

				if (totalRecived == totalSize) {
					intermediary.setString("done");
					return;
				}
			}

		} catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
			if (intermediary != null) {
				intermediary.fail(e);
			}

			if (currentFile != null) {
				currentFile.delete();
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
