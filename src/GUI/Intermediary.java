package GUI;

import javax.swing.JButton;
import javax.swing.JProgressBar;

public class Intermediary {
	private final JButton cancel;
	private final JProgressBar bar;

	public Intermediary(final JButton cancel, final JProgressBar fileProgress) {
		this.cancel = cancel;
		this.bar = fileProgress;
	}

	public void setString(final String string) {
		bar.setString(string);
	}

	public void setValue(final int value) {
		bar.setValue(value);
	}

	public void fail(Exception e) {
		cancel.setVisible(false);
		bar.setString("transfer failed (" + e.getMessage() + ")");
	}

	public void cancel() {
		bar.setString("Transfer cancelled!");
		cancel.setVisible(false);
	}
}
