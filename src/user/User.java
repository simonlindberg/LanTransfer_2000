package user;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JProgressBar;

import GUI.ChatPanel;
import GUI.Gui;
import chat.ChatReciver;
import chat.ChatSender;
import chat.ChatServerThread;

public class User {
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

	public User(final String username, final String ip, final Gui gui, final UserTable model) {
		this.ip = ip;
		this.username = username;
		this.model = model;
		this.chatPanel = new ChatPanel(this);
		chatPanel.setVisible(false);

		gui.addChatPanel(chatPanel);
		timestamp = System.currentTimeMillis();
	}

	private User(String username, String ip) {
		this.username = username;
		this.ip = ip;
		model = null;
		this.chatPanel = null;
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
		new ChatReciver(socket.getInputStream(), this).start();
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

				if (socket == null) {
					createNewChat();
				}
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
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
					} finally {
						socket = null;
					}
				}
				isOnline = false;
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
		if (socket == null) {
			createNewChat();
		}

		synchronized (messageLock) {
			if (unreadMessages) {
				unreadMessages = false;
				model.updateUser(this);
			}
		}
	}

	private void createNewChat() {
		try {
			socket = new Socket(ip, ChatServerThread.CHAT_PORT);
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

	public void sendMessage(final String text) {
		sender.send(text);
	}

	public void newMessage(final String msg) {
		chatPanel.showMessage(msg);

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

	public JProgressBar promptFileTransfer(final List<String> fileNames, final List<Long> fileSizes, final AtomicReference<File> savePlace) {
		return chatPanel.promptFileTransfer(fileNames, fileSizes, savePlace);
	}
}