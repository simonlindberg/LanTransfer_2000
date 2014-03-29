package main;

import java.io.InputStream;
import java.io.OutputStream;

public class User {

	private final String ip;
	private final String username;
	private final long arrived;
	private long latest;
	private int where;
	private InputStream in = null;
	private OutputStream out = null;

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

	public void setInputStream(InputStream inputStream) {
		in = inputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		out = outputStream;
	}
}
