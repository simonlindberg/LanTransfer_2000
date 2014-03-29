package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;

import GUI.GUI;
import broadcast.BroadcastListener;
import broadcast.BroadcastResponseHandler;
import broadcast.BroadcastSender;
import broadcast.BroadcastThread;

public class Main {
	private static final int CHECKER_TIMEOUT = 4000;// Time a user may have been
													// unactive before he is
													// kicked

	@SuppressWarnings({ "serial" })
	public static void main(String[] args) {
		final Object[][] data = { { "test1", "127.0.0.2" }, { "test2", "127.0.0.1" } };
		final String[] columnNames = { "Name", "IP" };

		final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		final List<User> users = new ArrayList<>();

		final Map<String, JComponent> clientWindows = new HashMap<String, JComponent>();

		try {
			final String username = System.getProperty("user.name");
			final String ip = InetAddress.getLocalHost().getHostAddress();

			final DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
			sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);

			final byte[] message = createMessage(username); // ELLER?

			final DatagramPacket sendPacket = new DatagramPacket(message, message.length);

			new BroadcastListener(sendSocket, sendPacket, new BroadcastResponseHandler() {

				@Override
				public void handle(final DatagramPacket packet) {
					final User user = new User(new String(packet.getData()), packet.getAddress().getHostAddress());
					synchronized (users) {
						if (users.contains(user)) {
							users.get(users.indexOf(user)).refresh();
						} else {
							users.add(user);
							model.addRow(new String[] { user.getUsername(), user.getIP() });
							user.setWhere(model.getRowCount() - 1);
						}
					}
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			final GUI gui = new GUI(username, ip, model, clientWindows, new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					sendForce(model, users, sendSocket, message, sendPacket);
				}
			});

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						for (;;) {
							synchronized (users) {
								final Iterator<User> itr = users.iterator();
								int removed = 0;
								while (itr.hasNext()) {
									final User user = itr.next();
									if ((System.currentTimeMillis() - user.getLatest()) > CHECKER_TIMEOUT) {
										itr.remove();
										model.removeRow(user.getWhere());
										gui.logOff(user);

										removed++;
									} else {
										user.setWhere(user.getWhere() - removed); // Update
																					// where
																					// the
																					// user
																					// is.
									}
								}
							}
							Thread.sleep(CHECKER_TIMEOUT);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			sendForce(model, users, sendSocket, message, sendPacket);
		} catch (SocketException | UnknownHostException e) {
			// GUI.showError("CRITICAL ERROR", e.getMessage() +
			// "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static void sendForce(final DefaultTableModel model, final List<User> users, final DatagramSocket sendSocket,
			final byte[] message, final DatagramPacket sendPacket) {
		System.out.println("sending force!");
		try {
			message[0] = 1; // FORCE ON!
			sendSocket.send(sendPacket);
			model.setRowCount(0);
			users.clear();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			message[0] = 0; // FORCE OFF!
		}
	}

	private static byte[] createMessage(final String name) {
		final byte[] nameBytes = name.getBytes();
		final byte[] message = new byte[nameBytes.length + 1];
		message[0] = 0; // NOT FORCED!
		System.arraycopy(nameBytes, 0, message, 1, nameBytes.length);
		return message;
	}
}
