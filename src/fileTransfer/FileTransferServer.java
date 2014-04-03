package fileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import user.User;

public class FileTransferServer extends Thread implements Runnable {

	public static final int FILETRANSFER_PORT = 10001;
	private Map<String, User> users;

	public FileTransferServer(final Map<String, User> users) {
		this.users = users;
	}

	@Override
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(FILETRANSFER_PORT);

			final Socket socket = ss.accept();
			System.out.println(socket.getInetAddress().getHostAddress());
			new FileTransferReciver(socket, users.get(socket.getInetAddress().getHostAddress())).start();
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
