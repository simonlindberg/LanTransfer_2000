package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatFrame extends JFrame {

	private static final long serialVersionUID = -2013659249126443168L;

	private User user;

	public ChatFrame(final User user) {
		this.user = user;
		setLayout(new BorderLayout());

		createComponents();

		setTitle("Chat window with " + user.getUsername());
		setSize(new Dimension(500, 600));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(100, 300));

		setVisible(true);

		// TCP
		System.out.println("Connecting......!!!!!11");
	}

	private void createComponents() {
		final JPanel chatLog = new JPanel();
		chatLog.setLayout(new BoxLayout(chatLog, BoxLayout.PAGE_AXIS));

		final JScrollPane scrollCharLog = new JScrollPane(chatLog);
		chatLog.setAutoscrolls(true);
		chatLog.setLayout(new BoxLayout(chatLog, BoxLayout.PAGE_AXIS));

		String startMessage = "Started chat with " + user.getUsername() + " at " + (new Date()).toString();
		chatLog.add(new JLabel(startMessage));
		add(scrollCharLog, BorderLayout.CENTER);

		final JPanel chatInput = new JPanel(new BorderLayout());
		final JTextField input = new JTextField();
		chatInput.add(input, BorderLayout.CENTER);
		final JButton send = new JButton("Send");
		chatInput.add(send, BorderLayout.EAST);
		add(chatInput, BorderLayout.SOUTH);

		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(input.getText(), chatLog);
					input.setText("");
				}
			}
		});

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				send(input.getText(), chatLog);
				input.setText("");
			}
		});
	}

	private void send(final String text, final JComponent chatLog) {
		if (!text.equals("")) {
			System.out.println(text);
			final JPanel messageContents = new JPanel(new FlowLayout());
			final JTextArea contents = new JTextArea(text);
			contents.setEditable(false);
			contents.setLineWrap(true);
			contents.setWrapStyleWord(true);

			contents.setBackground(Color.ORANGE);
			messageContents.add(contents);
			messageContents.setBackground(Color.BLUE);

			chatLog.add(messageContents);

			chatLog.revalidate();
			chatLog.repaint();
		}
	}

}
