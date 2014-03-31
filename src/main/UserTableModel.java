package main;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class UserTableModel extends DefaultTableModel implements UserTable {

	private List<User> users = new ArrayList<>();

	public UserTableModel() {
		super(null, new String[] { "Name", "IP" });
	}

	@Override
	public synchronized void removeUser(final User user) {
		final int index = users.indexOf(user);
		if (index >= 0) {
			users.remove(index);
			super.removeRow(index);
		}
	}

	@Override
	public synchronized void addUser(final User user) {
		users.add(user);
		super.addRow(new String[] { user.getUsername(), user.getIP() });
	}

	@Override
	public synchronized void clear() {
		super.setRowCount(0);
		users.clear();
	}

	@Override
	public boolean isCellEditable(int a, int b) {
		return false;
	}

}
