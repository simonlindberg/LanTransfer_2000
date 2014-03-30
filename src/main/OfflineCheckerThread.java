package main;

import java.util.Iterator;
import java.util.Map;

import GUI.Gui;

public class OfflineCheckerThread extends Thread implements Runnable {
	// Time a user may have been unactive before he is kicked
	private static final int CHECKER_TIMEOUT = 4000;

	private final ModelUpdater modelUpdater;
	private final Map<String, User> users;

	public OfflineCheckerThread(final Map<String, User> users, final ModelUpdater modelUpdater) {
		this.users = users;
		this.modelUpdater = modelUpdater;
	}

	@Override
	public void run() {
		try {
			for (;;) {
				final Iterator<User> itr = users.values().iterator();
				while (itr.hasNext()) {
					final User user = itr.next();
					if ((System.currentTimeMillis() - user.getLatest()) > CHECKER_TIMEOUT) {
						user.setOffline();
					}
				}
				// Update model!
				modelUpdater.update();

				Thread.sleep(CHECKER_TIMEOUT);
			}
		} catch (InterruptedException e) {
			Gui.showError("Critical error", e.getMessage() + "\n\nProgram will now exit.");
			System.exit(-1);
		}
	}

}