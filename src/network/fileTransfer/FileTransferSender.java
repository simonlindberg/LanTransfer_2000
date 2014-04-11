package network.fileTransfer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import network.NetworkUtils;

public class FileTransferSender extends Thread implements Runnable {

	private final List<Path> filePaths;
	private final String ip;
	private final FileTransferIntermediary intermediary;
	private long totalSize;
	private long totalSent;
	private DataOutputStream output;
	private long start;
	private final Socket socket;

	public FileTransferSender(final List<Path> filePaths, final String ip, final FileTransferIntermediary intermediary, final Socket socket) {
		this.intermediary = intermediary;
		this.socket = socket;
		this.filePaths = filePaths;
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
			socket.connect(new InetSocketAddress(ip, NetworkUtils.FILETRANSFER_PORT));

			final InputStream input = socket.getInputStream();
			output = new DataOutputStream(socket.getOutputStream());

			// Send number of files!
			output.writeLong(filePaths.size());

			for (final Path file : filePaths) {
				final long size = FileUtils.fileSize(file);
				output.writeUTF(file.getFileName().toString());
				output.writeLong(size);
				totalSize += size;
			}

			// Wait for OKEY!
			int response = input.read();
			while (response == NetworkUtils.NOTHING) {
				response = input.read();
			}

			if (response == NetworkUtils.CANCEL) {
				// cancelled
				intermediary.cancel();
				return;
			}

			// -1 since division by zero.
			start = System.currentTimeMillis() - 1;
			// Send actual file data
			sendFiles(filePaths, "");

			intermediary.done();

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

	private void sendFiles(final List<Path> paths, final String root) throws IOException {
		for (final Path file : paths) {
			if (FileUtils.isFile(file)) {
				sendFile(file, root + file.getFileName().toString());
			} else {
				sendFiles(FileUtils.folderContents(file), root + file.getFileName().toString() + "/");
			}
		}
	}

	private void sendFile(final Path file, final String filename) throws IOException {
		try (final InputStream in = Files.newInputStream(file)) {

			final long size = FileUtils.fileSize(file);

			output.writeUTF(filename); // Send file name!
			output.writeLong(size); // Send file size!

			final byte[] buffer = new byte[32768];

			long read = 0;
			while (read != size) {
				// Subtracting two longs and casting to int is bad, but it will
				// only ever happen
				// when the difference between the two is smaller than
				// buffer.length, which fits in an int
				// So this should be fine...
				final int toRead = (int) ((size - read) > buffer.length ? buffer.length : size - read);
				final int n = in.read(buffer, 0, toRead);
				output.write(buffer, 0, n);
				read = read + n;
				totalSent += n;

				final int percentage = (int) (100 * (totalSent / (double) totalSize));
				final long bytesPerMs = totalSent / (System.currentTimeMillis() - start) * 1000;

				intermediary.setValue(percentage);
				intermediary.setString(FileUtils.shorten(filename) + "  " + FileUtils.readbleTransferSpeed(bytesPerMs));
			}

		}
	}
}
