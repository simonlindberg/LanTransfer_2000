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

import user.User;
import user.UserTable;
import user.UserTableModel;
import GUI.Gui;
import broadcast.BroadcastListener;
import broadcast.BroadcastResponseHandler;
import broadcast.BroadcastSender;
import broadcast.BroadcastThread;
import chat.ChatInitiator;
import chat.ChatServerThread;

public class Main {

	public static final String myUsername = System.getProperty("user.name");
	public static final String myIP = getMyIP();

	public static void main(final String[] args) {
		try {
			final Map<String, User> users = new ConcurrentHashMap<String, User>();

			final UserTable model = new UserTableModel();

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

			new ChatServerThread(new ChatInitiator() {

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
						users.put(ip, new User(username, ip, gui, model));
					}
					
					final User user = users.get(ip);
					if (!user.isOnline()) {
						model.addUser(user);
					}
					user.setOnline();
				}

				@Override
				public void handleOfflineMessage(final DatagramPacket packet) {
					final String ip = packet.getAddress().getHostAddress();

					final User user = users.get(ip);

					if (user == null) {
						System.out.println("Unknow user sent offline message!");
						return;
					}

					user.setOffline();

					model.removeUser(user);
				}
			}).start();

			new BroadcastSender(sendSocket, sendPacket).start();

			new OfflineCheckerThread(users, model).start();

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
		} catch (SocketException e) {
			Gui.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static String getMyIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			Gui.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
			return "";
		}
	}

	private static void sendForce(final UserTable model, final Map<String, User> users, final DatagramSocket sendSocket,
			final byte[] message, final DatagramPacket sendPacket) {
		try {
			message[0] = BroadcastThread.FORCED; // FORCE ON!
			sendSocket.send(sendPacket);
			model.clear();
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
