package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer extends Thread implements Runnable {

	public static final int FILETRANSFER_PORT = 10001;
	private final FileTransferInitiator init;

	public FileTransferServer(final FileTransferInitiator init) {
		this.init = init;
	}

	@Override
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(FILETRANSFER_PORT);
			for (;;) {
				final Socket socket = ss.accept();
				init.initFileTransfer(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ss.close();
			} catch (Exception e) {
			}
		}
	}
}
