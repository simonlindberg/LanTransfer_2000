package GUI;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import main.Utils;
import network.fileTransfer.FileTransferIntermediary;

public class Intermediary implements FileTransferIntermediary {
	private final JButton cancel;
	private final JProgressBar bar;
	private final JButton saveAs;

	public Intermediary(final JButton cancel, final JButton saveAs, final JProgressBar fileProgress) {
		this.cancel = cancel;
		this.saveAs = saveAs;
		this.bar = fileProgress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GUI.FileTransferIntermediary#setString(java.lang.String)
	 */
	@Override
	public void setString(final String string) {
		bar.setString(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GUI.FileTransferIntermediary#setValue(int)
	 */
	@Override
	public void setValue(final int value) {
		bar.setValue(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GUI.FileTransferIntermediary#fail(java.lang.Exception)
	 */
	@Override
	public void fail(Exception e) {
		bar.setString("transfer failed (" + e.getMessage() + ")");
		hide();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GUI.FileTransferIntermediary#cancel()
	 */
	@Override
	public void cancel() {
		bar.setString("transfer cancelled!");
		hide();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see GUI.FileTransferIntermediary#done()
	 */
	@Override
	public void done() {
		bar.setString("done");
		hide();
		activateOpen();
	}

	private void activateOpen() {
		if (saveAs != null && (Utils.isMac() || Utils.isWindows())) {
			saveAs.setVisible(true);
			saveAs.setText("Open..");
		}
	}

	private void hide() {
		cancel.setVisible(false);
		if (saveAs != null) {
			saveAs.setVisible(false);
		}
	}
}
