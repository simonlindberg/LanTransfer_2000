package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import GUI.GUI;
import broadcast.BroadcastListener;
import broadcast.BroadcastResponseHandler;
import broadcast.BroadcastSender;
import broadcast.BroadcastThread;
import broadcast.User;

public class Main {
	private static final int TIMEOUT = 5000; // Time a user may have been
												// unactive before he is kicked

	@SuppressWarnings({ "serial" })
	public static void main(String[] args) {

		final DefaultTableModel model = new DefaultTableModel(null, new String[] { "Name", "IP" }) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		final List<User> users = new ArrayList<>();

		try {
			final DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
			sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);

			final byte[] message = createMessage(System.getProperty("user.name") + "the man!"); // ELLER?

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
							System.out.println("inside row: " + user.getWhere());
						}
					}
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						for (;;) {
							synchronized (users) {
								final Set<User> toRemove = new HashSet<>();
								for (final User user : users) {
									if ((System.currentTimeMillis() - user.getLatest()) > TIMEOUT) {
										model.removeRow(user.getWhere());
										toRemove.add(user);
									}
								}

								for (final User user : toRemove) {
									users.remove(user);
								}
							}
							Thread.sleep(TIMEOUT);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			new GUI(model, new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					System.out.println("sending force!");
					message[0] = 1; // FORCE ON!
					final DatagramPacket forcePacket = new DatagramPacket(message, message.length);
					try {
						sendSocket.send(forcePacket);
					} catch (IOException e1) {
						e1.printStackTrace();
					} finally {
						message[0] = 0; // FORCE OFF!
					}
				}
			});

		} catch (SocketException e) {
			GUI.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
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
