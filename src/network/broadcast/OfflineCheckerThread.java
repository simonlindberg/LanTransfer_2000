package network.broadcast;

import java.util.Iterator;
import java.util.Map;

import user.User;
import user.UserTable;
import GUI.Gui;

public class OfflineCheckerThread extends Thread implements Runnable {
	// Time a user may have been unactive before he is kicked
	private static final int CHECKER_TIMEOUT = 10000;

	private final UserTable model;
	private final Map<String, User> users;

	public OfflineCheckerThread(final Map<String, User> users, final UserTable model) {
		this.users = users;
		this.model = model;
	}

	@Override
	public void run() {
		try {
			for (;;) {
				final Iterator<User> itr = users.values().iterator();
				while (itr.hasNext()) {
					final User user = itr.next();
					if (user.isOnline() && (System.currentTimeMillis() - user.getLatest()) > CHECKER_TIMEOUT) {
						System.out.println("found: " + user.getUsername());
						user.setOffline();
						model.removeUser(user);
					}
				}

				Thread.sleep(CHECKER_TIMEOUT);
			}
		} catch (InterruptedException e) {
			Gui.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}

}