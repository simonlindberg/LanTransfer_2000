package main;

import java.net.DatagramPacket;
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
import broadcast.User;

public class Main {
	private static final int TIMEOUT = 1000; // Time a user may have been
												// unactive before he is kicked

	@SuppressWarnings("serial")
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
			final String name = System.getProperty("user.name"); // Eller ska
																	// man få
																	// välja?
			
			new BroadcastSender(name).start();

			new BroadcastListener(name, new BroadcastResponseHandler() {

				@Override
				public void handle(final DatagramPacket packet) {
					final User user = new User(new String(packet.getData()), packet.getAddress().getHostAddress());
					synchronized (users) {
						if (users.contains(user)) {
							users.get(users.indexOf(user)).refresh();
						} else {
							users.add(user);
							model.addRow(new String[] { user.getUsername(), user.getIP() });
							user.setWhere(model.getRowCount());
						}
					}
				}
			}).start();

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

		} catch (SocketException e) {
			GUI.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
		}

		new GUI(model);
	}
}
