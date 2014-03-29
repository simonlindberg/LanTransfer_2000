package main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import chat.ChatServerThread;
import chat.ChatThread;

public class User {

	private final String ip;
	private final String username;
	private final long arrived;

	private long latest;
	private int where;
	private Socket socket = null;
	private PrintWriter printWriter = null;

	public User(String username, String ip) {
		this.ip = ip;
		this.username = username;
		arrived = System.currentTimeMillis();
		latest = arrived;
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

	public void refresh() {
		latest = System.currentTimeMillis();
	}

	public long getLatest() {
		return latest;
	}

	public void setWhere(int where) {
		this.where = where;
	}

	public int getWhere() {
		return where;
	}

	public void startChat() {
		try {
			socket = new Socket(ip, ChatServerThread.CHAT_PORT);
			socket.getOutputStream().write(-1);

			initWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initWriter() {
		try {
			printWriter = new PrintWriter(socket.getOutputStream());
			new ChatThread(socket.getInputStream()).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startChat(final Socket socket) {
		this.socket = socket;

		initWriter();
	}

	public void setOffline() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean chatInitiated() {
		return socket != null;
	}

	public void send(final String text) {
		printWriter.print(text);
	}

}
