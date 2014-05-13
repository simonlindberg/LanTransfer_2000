package user;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class UserTableModel extends DefaultTableModel implements UserTable {

	// A light gray. Very fab!
	private static final Color UNEVEN_ROW_COLOR = new Color(235, 235, 235);
	private static final Color EVEN_ROW_COLOR = Color.WHITE;

	private final List<User> users = new ArrayList<>();

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
	public boolean isCellEditable(final int a, final int b) {
		//Never edit, never surrender!
		return false;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
			final boolean hasFocus, final int row, final int column) {
		final JLabel label = new JLabel((String) value);
		label.setOpaque(true);

		if (row % 2 == 0) {
			label.setBackground(EVEN_ROW_COLOR);
		} else {
			label.setBackground(UNEVEN_ROW_COLOR);
		}

		if (isSelected) {
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		}

		final User user = users.get(row);
		if (user != null && user.hasUnreadMessages()) {
			label.setForeground(Color.MAGENTA);
		}
		return label;
	}

	@Override
	public void updateUser(final User user) {
		final int index = users.indexOf(user);
		if (index >= 0) {
			super.fireTableCellUpdated(index, 0);
			super.fireTableCellUpdated(index, 1);
		}
	}
}
