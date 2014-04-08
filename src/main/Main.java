package main;

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
import broadcast.OfflineCheckerThread;
import chat.ChatInitiator;
import chat.ChatServerThread;
import fileTransfer.FileTransferServer;

public class Main {

	public static final String myUsername = System.getProperty("user.name");
	public static final String myIP = getMyIP();

	private static final Map<String, User> users = new ConcurrentHashMap<String, User>();
	private static final UserTable model = new UserTableModel();
	private static final byte[] message = createMessage();
	private static final DatagramPacket sendPacket = new DatagramPacket(message, message.length);

	public static void main(final String[] args) {
		try {
			final DatagramSocket sendSocket = createBroadcastSendSocket();
			final Gui gui = createGUI();

			startChatServer();
			startTransferServer();

			startOfflineChecker();

			startBroadcastListener(sendSocket, gui);
			startBroadcastSender(sendSocket);

			addShutdownHook(sendSocket);
		} catch (SocketException e) {
			Gui.showError("CRITICAL ERROR", e.getMessage() + "\n\nShuting down.");
			System.exit(-1);
		}

	}

	private static DatagramSocket createBroadcastSendSocket() throws SocketException {
		final DatagramSocket sendSocket = new DatagramSocket();
		sendSocket.setBroadcast(true); // Behövs defacto inte, men why not.
		sendSocket.connect(BroadcastThread.getBroadcastAddress(), BroadcastThread.BROADCAST_PORT);
		return sendSocket;
	}

	private static void startOfflineChecker() {
		new OfflineCheckerThread(users, model).start();
	}

	private static void startTransferServer() {
		new FileTransferServer(users).start();
	}

	private static void addShutdownHook(final DatagramSocket sendSocket) {
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
	}

	private static void startBroadcastSender(final DatagramSocket sendSocket) {
		new BroadcastSender(sendSocket, sendPacket, message).start();
	}

	private static void startBroadcastListener(final DatagramSocket sendSocket, final Gui gui) {
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
	}

	private static void startChatServer() {
		new ChatServerThread(new ChatInitiator() {

			@Override
			public void initChat(final Socket socket) throws IOException {
				final String ip = socket.getInetAddress().getHostAddress();

				final User user = users.get(ip);

				if (user == null) {
					System.out.println("Unknow user tried to connect a chat: " + ip);
					return;
				}

				user.newChat(socket);
			}
		}).start();
	}

	private static Gui createGUI() {
		return new Gui(model, users);
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

	private static byte[] createMessage() {
		final byte[] nameBytes = myUsername.getBytes();
		final byte[] message = new byte[nameBytes.length + 1];
		message[0] = BroadcastThread.NORMAL; // NOT FORCED!
		System.arraycopy(nameBytes, 0, message, 1, nameBytes.length);
		return message;
	}
}
