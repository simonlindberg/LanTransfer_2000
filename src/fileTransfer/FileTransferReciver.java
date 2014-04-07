package fileTransfer;

import java.io.BufferedOutputStream;
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

import javax.swing.JProgressBar;

import user.User;

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
		 * 1. Ta emot antal
		 * 2. För alla filer: ta emot filnamn och storlek.
		 * 3. Fråga användaren JA/NEJ
		 * 4a. (NEJ) Skicka NEJ (0).
		 * 4b. (JA) Skicka OKEY (1).
		 * 5. För alla filer: ta emot filnamn, storlek OCH data.
		 */
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

			final JProgressBar progressBar = user.promptFileTransfer(fileNames, fileSizes, savePlace, latch);

			latch.await(); // Wait for user interaction!

			final File folder = savePlace.get();

			// User cancelled.
			if (folder == null) {
				socket.getOutputStream().write(CANCEL);
				progressBar.setString("transfer cancelled");
				return;
			}

			// Send OKEY!
			socket.getOutputStream().write(ACCEPT);

			// Total amount recived.
			int recived = 0;
			final long start = System.currentTimeMillis();
			for (;;) {
				final String filename = input.readUTF();
				final int size = input.readInt();

				final byte[] buffer = new byte[2048];

				final File file = new File(folder, filename);

				FileUtils.createParentFolders(file);

				final OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));

				int read = 0;
				while (read != size) {
					final int toRead = (size - read) > buffer.length ? buffer.length : size - read;
					final int n = input.read(buffer, 0, toRead);
					fos.write(buffer, 0, n);
					read = read + n;
					recived += n;
					progressBar.setValue((int) (100 * (recived / (double) totalSize)));

					long bytesPerMs = recived / (System.currentTimeMillis() - start);
					progressBar.setString(filename + "  " + bytesPerMs + " kb/s");
				}

				fos.close();
			}

		} catch (IOException | InterruptedException e) {
			System.out.println("it's okey. No more files.");
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
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
