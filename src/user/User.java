package user;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;

import network.NetworkUtils;
import network.chat.ChatReciverThread;
import network.chat.ChatSender;
import network.chat.MessageReciver;
import network.fileTransfer.FileTransferIntermediary;
import network.fileTransfer.FileTransferPrompter;
import GUI.ChatPanel;
import GUI.Gui;

public class User implements MessageReciver, FileTransferPrompter {
	public final static User NULL_USER = new User("", "");

	private final String ip;
	private final String username;
	private final ChatPanel chatPanel;

	private ChatSender sender;
	private Socket socket;

	private long timestamp;
	private boolean isOnline;

	private final Object onlineLock = new Object();
	private final Object messageLock = new Object();

	private boolean unreadMessages;

	private final UserTable model;

	private final Map<Integer, JLabel> myMessageStatuses;

	private final Set<Integer> unseenMessages;

	public User(final String username, final String ip, final Gui gui, final UserTable model) {
		this.ip = ip;
		this.username = username;
		this.model = model;
		this.chatPanel = new ChatPanel(this);
		this.myMessageStatuses = new HashMap<>();
		this.unseenMessages = new HashSet<>();
		chatPanel.setVisible(false);

		gui.addChatPanel(chatPanel);
		timestamp = System.currentTimeMillis();
	}

	private User(String username, String ip) {
		this.username = username;
		this.ip = ip;
		model = null;
		this.chatPanel = null;
		myMessageStatuses = null;
		this.unseenMessages = new HashSet<>();
	}

	public String toString() {
		return username + " (" + ip + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User other = (User) obj;
			return ip.equals(other.ip);
		}
		return false;
	}

	public String getUsername() {
		return username;
	}

	public String getIP() {
		return ip;
	}

	private void refresh() {
		timestamp = System.currentTimeMillis();
	}

	public long getLatest() {
		synchronized (onlineLock) {
			return timestamp;
		}
	}

	/**
	 * Initiates a new chat with this user.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public void newChat(final Socket socket) throws IOException {
		this.socket = socket;
		initChat();
	}

	private void initChat() throws IOException {
		sender = new ChatSender(socket.getOutputStream());
		new ChatReciverThread(socket.getInputStream(), socket.getOutputStream(), this).start();
	}

	/**
	 * Sets a user to be online. If this is the first time, then a chatPanel
	 * will be initiated.
	 */
	public void setOnline() {
		synchronized (onlineLock) {
			refresh();
			if (!isOnline) {
				chatPanel.setOnline();
				isOnline = true;
			}
		}
	}

	/**
	 * Sets a user to be offline.
	 */
	public void setOffline() {
		synchronized (onlineLock) {
			if (isOnline) {
				chatPanel.setOffline();
				isOnline = false;
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
					} finally {
						socket = null;
					}
				}
			}
		}
	}

	/**
	 * Hides the chat, enabling some other user to show.
	 */
	public void hideChat() {
		if (chatPanel != null) {
			chatPanel.setVisible(false);
		}
	}

	/**
	 * Shows the chat for this user.
	 */
	public void showChat() {
		chatPanel.setVisible(true);

		for (final int id : unseenMessages) {
			try {
				sender.sendSeenConfirm(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		myMessageStatuses.clear();

		synchronized (messageLock) {
			if (unreadMessages) {
				unreadMessages = false;
				model.updateUser(this);
			}
		}
	}

	private void createNewChat() {
		try {
			socket = new Socket(ip, NetworkUtils.CHAT_PORT);
			initChat();
		} catch (IOException e) {
			chatPanel.showMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean isOnline() {
		synchronized (onlineLock) {
			return isOnline;
		}
	}

	public void sendMessage(final String text, final JLabel status) {
		if (socket == null || socket.isClosed()) {
			createNewChat();
		}

		// TA HAND OM DETTA KANSKE?!
		try {
			final int id = sender.sendMessage(text);
			myMessageStatuses.put(id, status);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateUI() {
		synchronized (messageLock) {
			if (!chatPanel.isVisible() && !unreadMessages) {
				unreadMessages = true;
				model.updateUser(this);
			}
		}
	}

	public boolean hasUnreadMessages() {
		synchronized (messageLock) {
			return unreadMessages;
		}
	}

	public FileTransferIntermediary promptFileTransfer(final List<String> fileNames, final List<Long> fileSizes,
			final AtomicReference<String> savePlace, final CountDownLatch latch, final Socket socket) {
		final FileTransferIntermediary intermediary = chatPanel.promptFileTransfer(fileNames, fileSizes, savePlace, latch, socket);

		updateUI();
		return intermediary;
	}

	@Override
	public void newMessage(final String msg, final int id) {
		System.out.println("new: " + id);
		if (chatPanel.isVisible()) {
			try {
				sender.sendSeenConfirm(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			unseenMessages.add(id);
		}
		updateUI();
	}

	@Override
	public void recivedMessage(final int id) {
		myMessageStatuses.get(id).setText("recived");
		System.out.println("recived: " + id);
	}

	@Override
	public void seenMessage(final int id) {
		myMessageStatuses.get(id).setText("seen");
		System.out.println("seen: " + id);
	}
}
