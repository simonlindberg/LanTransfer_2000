package main;

import java.util.Iterator;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import GUI.GUI;

public class OfflineCheckerThread extends Thread implements Runnable {
	// Time a user may have been unactive before he is kicked
	private static final int CHECKER_TIMEOUT = 4000;

	private final DefaultTableModel model;
	private final List<User> users;
	private final GUI gui;

	public OfflineCheckerThread(final List<User> users, final DefaultTableModel model, final GUI gui) {
		this.users = users;
		this.model = model;
		this.gui = gui;
	}

	@Override
	public void run() {
		try {
			for (;;) {
				synchronized (users) {
					final Iterator<User> itr = users.iterator();
					int removed = 0;
					while (itr.hasNext()) {
						final User user = itr.next();
						if ((System.currentTimeMillis() - user.getLatest()) > CHECKER_TIMEOUT) {
							itr.remove();
							model.removeRow(user.getWhere());
							gui.logOff(user);

							removed++;
						} else {
							user.setWhere(user.getWhere() - removed); // Update
																		// where
																		// the
																		// user
																		// is.
						}
					}
				}
				Thread.sleep(CHECKER_TIMEOUT);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}