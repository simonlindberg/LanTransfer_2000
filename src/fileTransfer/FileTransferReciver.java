package fileTransfer;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileTransferReciver extends Thread implements Runnable {

	private final Socket socket;

	public FileTransferReciver(final Socket socket) {
		this.socket = socket;
		System.out.println("construct");
	}

	@Override
	public void run() {
		/*
		 * 1. Ta emot antal
		 * 2. För alla filer: ta emot filnamn och storlek.
		 * 3. Fråga användaren JA/NEJ
		 * 4a. (NEJ) Stäng socket.
		 * 4b. (JA) Skicka OKEY.
		 * 5. För alla filer: ta emot filnamn, storlek OCH data.
		 */
		try {
			final DataInputStream input = new DataInputStream(socket.getInputStream());

			final long numberOfFiles = input.readLong();

			for (int i = 0; i < numberOfFiles; i++) {
				System.out.println(input.readUTF()); // Filename
				System.out.println(input.readLong()); // File size
			}

			// Prompt user...

			// Send OKEY!
			socket.getOutputStream().write(1);

			for (;;) {
				final String filename = input.readUTF();
				final int size = input.readInt();

				final byte[] buffer = new byte[2048];

				final File file = new File(filename);

				final OutputStream fos = new BufferedOutputStream(new FileOutputStream(file));

				int read = 0;
				while (read != size) {
					final int toRead = (size - read) > buffer.length ? buffer.length : size - read;
					final int n = input.read(buffer, 0, toRead);
					fos.write(buffer, 0, n);
					read = read + n;
				}

				fos.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
