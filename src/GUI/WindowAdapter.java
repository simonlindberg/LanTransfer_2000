package GUI;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public abstract class WindowAdapter implements WindowListener {

	@Override
	final public void windowOpened(WindowEvent e) {
	}

	@Override
	public abstract void windowClosing(WindowEvent e);

	@Override
	final public void windowClosed(WindowEvent e) {
	}

	@Override
	final public void windowIconified(WindowEvent e) {
	}

	@Override
	final public void windowDeiconified(WindowEvent e) {
	}

	@Override
	final public void windowActivated(WindowEvent e) {
	}

	@Override
	final public void windowDeactivated(WindowEvent e) {
	}

}
