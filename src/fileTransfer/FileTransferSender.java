package fileTransfer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import GUI.Intermediary;

public class FileTransferSender extends Thread implements Runnable {

	private final List<File> files;
	private final String ip;
	private final Intermediary intermediary;
	private long totalSize;
	private long totalSent;
	private DataOutputStream output;
	private long start;
	private final Socket socket;

	public FileTransferSender(final List<File> files, final String ip, final Intermediary intermediary, final Socket socket) {
		this.intermediary = intermediary;
		this.socket = socket;
		this.files = files;
		this.ip = ip;
	}

	@Override
	public void run() {
		/*
		 * 1. Skicka antal
		 * 2. För alla filer: skica filnamn och storlek.
		 * 3. Vänta på svar
		 * 4a. Om NEJ (0), stäng ner.
		 * 4b. Om OKEY (1), fortsätt och börja skicka.
		 * 5. För alla filer: skicka filnamn, storlek OCH data.
		 */
		try {
			socket.connect(new InetSocketAddress(ip, FileTransferServer.FILETRANSFER_PORT));

			final InputStream input = socket.getInputStream();
			output = new DataOutputStream(socket.getOutputStream());

			// Send number of files!
			output.writeLong(files.size());

			for (final File file : files) {
				final long size = FileUtils.fileSize(file);
				output.writeUTF(file.getName());
				output.writeLong(size);
				totalSize += size;
			}

			// Wait for OKEY!
			final int response = input.read();

			if (response == FileTransferReciver.CANCEL) {
				// cancelled
				intermediary.cancel();
				return;
			}

			// -1 since division by zero.
			start = System.currentTimeMillis() - 1;
			// Send actual file data
			sendFiles(files.toArray(new File[0]), "");

			intermediary.setString("done");

			// End of files --> end of stream.
			socket.close();
		} catch (IOException e) {
			intermediary.fail(e);
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	private void sendFiles(final File[] files, final String root) throws IOException {
		for (final File file : files) {
			if (FileUtils.isFile(file)) {
				sendFile(file, root + file.getName());
			} else {
				sendFiles(file.listFiles(), root + file.getName() + "/");
			}
		}
	}

	private void sendFile(final File file, final String filename) throws IOException {
		try (final InputStream in = new FileInputStream(file)) {

			final long size = file.length();

			output.writeUTF(filename); // Send file name!
			output.writeLong(size); // Send file size!

			final byte[] buffer = new byte[32768];

			long read = 0;
			while (read != size) {
				// Subtracting two longs and casting to int is bad, but it will only ever happen
				// when the difference between the two is smaller than buffer.length, which fits in an int
				// So this should be fine...
				final int toRead = (int) ((size - read) > buffer.length ? buffer.length : size - read);
				final int n = in.read(buffer, 0, toRead);
				output.write(buffer, 0, n);
				read = read + n;
				totalSent += n;

				final int percentage = (int) (100 * (totalSent / (double) totalSize));
				final long bytesPerMs = totalSent / (System.currentTimeMillis() - start) * 1000;

				intermediary.setValue(percentage);
				intermediary.setString(filename + "  " + FileUtils.readbleTransferSpeed(bytesPerMs));
			}

		}
	}
}
