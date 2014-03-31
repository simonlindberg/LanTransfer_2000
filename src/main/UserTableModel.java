package main;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
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

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final JLabel jLabel = new JLabel((String) value);
		final User user = users.get(row);
		if (user.hasUnreadMessages()) {
			jLabel.setForeground(Color.MAGENTA);
		}
		System.out.println("repaint!");
		return jLabel;
	}

	@Override
	public void updateUser(final User user) {
		final int index = users.indexOf(user);
		System.out.println("update: " + index);
		if (index >= 0) {
			super.fireTableCellUpdated(index, 0);
			super.fireTableCellUpdated(index, 1);
		}
	}

}
