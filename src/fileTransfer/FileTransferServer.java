package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer extends Thread implements Runnable {

	public static final int FILETRANSFER_PORT = 10000;

	@Override
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(FILETRANSFER_PORT);

			final Socket socket = ss.accept();
			System.out.println("server?");
			new FileTransferReciver(socket).start();
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
