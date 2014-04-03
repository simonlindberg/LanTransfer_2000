package fileTransfer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import javax.swing.JProgressBar;

public class FileTransferSender extends Thread implements Runnable {

	private final List<File> files;
	private final String ip;
	private final JProgressBar progressBar;
	private long totalSize;
	private long sent;
	private DataOutputStream output;

	public FileTransferSender(final List<File> files, final String ip, final JProgressBar fileProgress) {
		this.progressBar = fileProgress;
		this.files = files;
		this.ip = ip;
	}

	@Override
	public void run() {
		/*
		 * 1. Skicka antal
		 * 2. För alla filer: skica filnamn och storlek.
		 * 3. Vänta på JA
		 * 4. För alla filer: skicka filnamn, storlek OCH data.
		 */
		try {
			final Socket socket = new Socket(ip, FileTransferServer.FILETRANSFER_PORT);
			final InputStream input = socket.getInputStream();
			output = new DataOutputStream(socket.getOutputStream());

			// Send number of files!
			output.writeLong(files.size());

			for (final File file : files) {
				final long size = Utils.fileSize(file);
				output.writeUTF(file.getName());
				output.writeLong(size);
				totalSize += size;
			}

			// Wait for OKEY!
			input.read();

			// Send actual file data
			sendFiles(files.toArray(new File[0]), "");

			// End of files --> end of stream.
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFiles(final File[] files, final String root) throws IOException {
		for (final File file : files) {
			if (file.isFile()) {
				sendFile(file, root + file.getName());
			} else {
				sendFiles(file.listFiles(), root + file.getName() + "/");
			}
		}
	}

	private void sendFile(final File file, final String filename) throws IOException {
		final int size = (int) file.length();

		output.writeUTF(filename); // Send file name!
		output.writeInt(size); // Send file size!

		final InputStream in = new FileInputStream(file);

		final byte[] buffer = new byte[2048];

		int read = 0;
		while (read != size) {
			final int toRead = (size - read) > buffer.length ? buffer.length : size - read;
			final int n = in.read(buffer, 0, toRead);
			output.write(buffer, 0, n);
			read = read + n;
			sent += n;
			progressBar.setValue((int) (100 * (sent / (double) totalSize)));
		}

		in.close();
	}
}
