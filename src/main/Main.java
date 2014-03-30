package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import GUI.ChatPanel;
import GUI.GUI;
import broadcast.BroadcastListener;
import broadcast.BroadcastResponseHandler;
import broadcast.BroadcastSender;
import broadcast.BroadcastThread;
import chat.ChatHandler;
import chat.ChatServerThread;

public class Main {

	@SuppressWarnings({ "serial" })
	public static void main(String[] args) {

		try {
			final DefaultTableModel model = new DefaultTableModel(null, new String[] { "Name", "IP" }) {
				@Override
				public boolean isCellEditable(int row, int column) {
					// all cells false
					return false;
				}
			};

			final Map<String, User> users = new HashMap<String, User>();
			final Map<String, ChatPanel> clientWindows = new HashMap<>();

			final String myUsername = System.getProperty("user.name");
			final String myIP = InetAddress.getLocalHost().getHostAddress();

			final DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
			sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);

			final byte[] message = createMessage(myUsername); // ELLER?

			final DatagramPacket sendPacket = new DatagramPacket(message, message.length);

			final GUI gui = new GUI(myUsername, myIP, model, clientWindows, new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					sendForce(model, users, sendSocket, message, sendPacket);
				}
			});

			new ChatServerThread(new ChatHandler() {

				@Override
				public void initChat(final Socket socket) {
					try {
						final String ip = socket.getInetAddress().getHostAddress();

						synchronized (clientWindows) {
							if (!clientWindows.containsKey(ip)) {

								synchronized (users) {
									final User user = users.get(ip);
									clientWindows.put(ip, new ChatPanel(myUsername, user));
								}
							}
							final ChatPanel chatPanel = clientWindows.get(ip);
							gui.addChatPanel(chatPanel);
							chatPanel.setSocket(socket);
						}
					} catch (IOException e) {
						System.out.println("Attempted to set socket for user but failed");
						e.printStackTrace();
					}
				}
			}).start();

			new BroadcastListener(sendSocket, sendPacket, new BroadcastResponseHandler() {

				@Override
				public void handleBroadcast(final DatagramPacket packet) {
					final String ip = packet.getAddress().getHostAddress();

					synchronized (clientWindows) {
						if (clientWindows.containsKey(ip)) {
							clientWindows.get(ip).setOnline();
						}
					}

					final User user = new User(new String(packet.getData(), 0, packet.getLength()), ip);
					synchronized (users) {
						if (users.containsValue(user)) {
							users.get(ip).refresh();
						} else {
							users.put(ip, user);
							model.addRow(new String[] { user.getUsername(), ip });
						}
					}
				}

				@Override
				public void handleGoingOffline(final DatagramPacket packet) {
					final String ip = packet.getAddress().getHostAddress();
					synchronized (users) {
						final User user = users.get(ip);
						users.remove(ip);
						gui.logOff(user);
						model.setRowCount(0);
						for (final User remainingUser : users.values()) {
							model.addRow(new String[] { remainingUser.getUsername(), remainingUser.getIP() });
						}
					}
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			new OfflineCheckerThread(users, model, gui).start();

			sendForce(model, users, sendSocket, message, sendPacket);

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					message[0] = BroadcastThread.GOING_OFFLINE;
					try {
						sendSocket.send(sendPacket);
					} catch (IOException e) {
					}
				}
			}));
		} catch (SocketException | UnknownHostException e) {
			GUI.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static void sendForce(final DefaultTableModel model, final Map<String, User> users, final DatagramSocket sendSocket,
			final byte[] message, final DatagramPacket sendPacket) {
		try {
			message[0] = BroadcastThread.FORCED; // FORCE ON!
			sendSocket.send(sendPacket);
			model.setRowCount(0);
			users.clear();
		} catch (IOException e) {
			System.out.println("Tried to send broadcast but failed.");
			e.printStackTrace();
		} finally {
			message[0] = BroadcastThread.NORMAL; // FORCE OFF!
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
