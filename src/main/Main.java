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
import chat.ChatReciver;
import chat.Sender;

public class Main {

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

		final Map<String, User> users = new HashMap<String, User>();

		final Map<String, ChatPanel> clientWindows = new HashMap<>();

		try {
			final String myUsername = System.getProperty("user.name");
			final String myIP = InetAddress.getLocalHost().getHostAddress();

			final DatagramSocket sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
			sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);

			final byte[] message = createMessage(myUsername); // ELLER?

			final DatagramPacket sendPacket = new DatagramPacket(message, message.length);

			new BroadcastListener(sendSocket, sendPacket, new BroadcastResponseHandler() {

				@Override
				public void handle(final DatagramPacket packet) {
					final User user = new User(new String(packet.getData(), 0, packet.getLength()), packet.getAddress().getHostAddress());
					synchronized (users) {
						if (users.containsValue(user)) {
							users.get(user.getIP()).refresh();
						} else {
							users.put(user.getIP(), user);
							model.addRow(new String[] { user.getUsername(), user.getIP() });
							user.setWhere(model.getRowCount() - 1);
						}
					}
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			final GUI gui = new GUI(myUsername, myIP, model, clientWindows, new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					sendForce(model, users, sendSocket, message, sendPacket);
				}
			});

			new OfflineCheckerThread(users, model, gui).start();

			sendForce(model, users, sendSocket, message, sendPacket);

			new ChatServerThread(new ChatHandler() {

				@Override
				public void initChat(final Socket socket) {
					try {
						final String ip = socket.getInetAddress().getHostAddress();
						if (!clientWindows.containsKey(ip)) {
							final User user = users.get(ip);
							clientWindows.put(ip, new ChatPanel(myUsername, user));
						}
						final ChatPanel chatPanel = clientWindows.get(ip);

						chatPanel.setSender(new Sender(socket.getOutputStream()));

						new ChatReciver(socket.getInputStream(), chatPanel).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (SocketException | UnknownHostException e) {
			// GUI.showError("CRITICAL ERROR", e.getMessage() +
			// "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static void sendForce(final DefaultTableModel model, final Map<String, User> users, final DatagramSocket sendSocket,
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
