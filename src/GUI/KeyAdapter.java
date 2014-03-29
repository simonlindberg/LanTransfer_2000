package GUI;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class KeyAdapter implements KeyListener {

	@Override
	final public void keyTyped(KeyEvent e) {
	}

	@Override
	public abstract void keyPressed(KeyEvent e);

	@Override
	final public void keyReleased(KeyEvent e) {
	}

}
