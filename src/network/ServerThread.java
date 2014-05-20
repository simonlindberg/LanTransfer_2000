package network;

import java.io.IOException;
import java.net.ServerSocket;

import GUI.Gui;

public class ServerThread extends Thread implements Runnable {

	private final Initiator initiator;
	private final int port;

	public ServerThread(final int port, final Initiator initiator) {
		this.port = port;
		this.initiator = initiator;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			final ServerSocket server = new ServerSocket(port);
			for (;;) {
				initiator.init(server.accept());
			}
		} catch (IOException e) {
			Gui.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}
}
