package GUI;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

@SuppressWarnings("serial")
public class HintTextField extends JTextField implements FocusListener {
	private static final Color hintColor = Color.GRAY;
	private static final Color normalColor = Color.BLACK;
	private final String hint;
	private boolean hinting;

	public HintTextField(final int size, final String hint) {
		super(size);
		this.hint = hint;
		addFocusListener(this);
		focusLost(null);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (getText().isEmpty()) {
			setForeground(normalColor);
			setText("");
			hinting = false;
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (getText().isEmpty()) {
			setForeground(hintColor);
			setText(hint);
			hinting = true;
		}
	}

	@Override
	public String getText() {
		return hinting ? "" : super.getText();
	}

}
