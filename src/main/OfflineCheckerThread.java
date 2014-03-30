package main;

import java.util.Iterator;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import GUI.GUI;

public class OfflineCheckerThread extends Thread implements Runnable {
	// Time a user may have been unactive before he is kicked
	private static final int CHECKER_TIMEOUT = 4000;

	private final DefaultTableModel model;
	private final Map<String, User> users;
	private final GUI gui;

	public OfflineCheckerThread(final Map<String, User> users, final DefaultTableModel model, final GUI gui) {
		this.users = users;
		this.model = model;
		this.gui = gui;
	}

	@Override
	public void run() {
		try {
			for (;;) {
				final Iterator<User> itr = users.values().iterator();
				while (itr.hasNext()) {
					final User user = itr.next();
					if ((System.currentTimeMillis() - user.getLatest()) > CHECKER_TIMEOUT) {
						itr.remove();
						gui.logOff(user);
					}
				}
				// Reset model!
				model.setRowCount(0);
				for (final User user : users.values()) {
					model.addRow(new String[] { user.getUsername(), user.getIP() });
				}
				Thread.sleep(CHECKER_TIMEOUT);
			}
		} catch (InterruptedException e) {
			GUI.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}

}