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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.DefaultTableModel;

import GUI.Gui;
import broadcast.BroadcastListener;
import broadcast.BroadcastResponseHandler;
import broadcast.BroadcastSender;
import broadcast.BroadcastThread;
import chat.ChatHandler;
import chat.ChatServerThread;

public class Main {
	public static final String myUsername = System.getProperty("user.name");
	public static String myIP;

	@SuppressWarnings({ "serial" })
	public static void main(String[] args) {

		try {
			final Map<String, User> users = new ConcurrentHashMap<String, User>();
			myIP = InetAddress.getLocalHost().getHostAddress();

			final DefaultTableModel model = new DefaultTableModel(null, new String[] { "Name", "IP" }) {
				@Override
				public boolean isCellEditable(int row, int column) {
					// all cells false
					return false;
				}
			};

			final DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
			sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);

			final byte[] message = createMessage();

			final DatagramPacket sendPacket = new DatagramPacket(message, message.length);

			final Gui gui = new Gui(model, users, new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					sendForce(model, users, sendSocket, message, sendPacket);
				}
			});

			new ChatServerThread(new ChatHandler() {

				@Override
				public void initChat(final Socket socket) throws IOException {
					final String ip = socket.getInetAddress().getHostAddress();

					final User user = users.get(ip);

					if (user == null) {
						System.out.println("Unknow user tried to connect a chat!");
						return;
					}

					user.newChat(socket);
				}
			}).start();

			new BroadcastListener(sendSocket, sendPacket, new BroadcastResponseHandler() {

				@Override
				public void handleBroadcast(final DatagramPacket packet) {
					final String ip = packet.getAddress().getHostAddress();
					final String username = new String(packet.getData(), 1, packet.getLength() - 1);

					if (!users.containsKey(ip)) {
						users.put(ip, new User(username, ip));
						model.addRow(new String[] { username, ip });
					}
					final User user = users.get(ip);
					user.refresh();
					user.setOnline(gui);
				}

				@Override
				public void handleOfflineMessage(final DatagramPacket packet) {
					System.out.println("offline!");
					final String ip = packet.getAddress().getHostAddress();

					final User user = users.get(ip);

					if (user == null) {
						System.out.println("Unknow user sent offline message!");
						return;
					}

					user.setOffline();

					updateModel(model, users);
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			new OfflineCheckerThread(users, new ModelUpdater() {

				@Override
				public void update() {
					updateModel(model, users);
				}

			}).start();

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
			Gui.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static void updateModel(final DefaultTableModel model, final Map<String, User> users) {
		model.setRowCount(0);
		for (final User u : users.values()) {
			if (u.isOnline()) {
				model.addRow(new String[] { u.getUsername(), u.getIP() });
			}
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

	private static byte[] createMessage() {
		final byte[] nameBytes = myUsername.getBytes();
		final byte[] message = new byte[nameBytes.length + 1];
		message[0] = 0; // NOT FORCED!
		System.arraycopy(nameBytes, 0, message, 1, nameBytes.length);
		return message;
	}
}
