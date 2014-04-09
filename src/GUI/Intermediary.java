package GUI;

import javax.swing.JButton;
import javax.swing.JProgressBar;

public class Intermediary {
	private final JButton cancel;
	private final JProgressBar bar;
	private final JButton saveAs;

	public Intermediary(final JButton cancel, final JButton saveAs, final JProgressBar fileProgress) {
		this.cancel = cancel;
		this.saveAs = saveAs;
		this.bar = fileProgress;
	}

	public void setString(final String string) {
		bar.setString(string);
	}

	public void setValue(final int value) {
		bar.setValue(value);
	}

	public void fail(Exception e) {
		bar.setString("transfer failed (" + e.getMessage() + ")");
		hide();
	}

	public void cancel() {
		bar.setString("transfer cancelled!");
		hide();
	}

	public void done() {
		bar.setString("done");
		hide();
	}

	private void hide() {
		cancel.setVisible(false);
		if (saveAs != null) {
			saveAs.setVisible(false);
		}
	}
}
