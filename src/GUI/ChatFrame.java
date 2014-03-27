package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
	private int chatNum;

	public ChatFrame(final User user) {
		this.user = user;
		chatNum = 0;
		setLayout(new BorderLayout());

		createComponents();

		setTitle("Chat window with " + user.getUsername());
		setSize(new Dimension(500, 600));
		setResizable(false);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setVisible(true);

		// TCP
		System.out.println("Connecting......!!!!!11");
	}

	private void createComponents() {
		final JPanel chatLog = new JPanel(new GridBagLayout());
		// chatLog.setLayout(new BoxLayout(chatLog, BoxLayout.PAGE_AXIS));

		final JScrollPane scrollChatLog = new JScrollPane(chatLog);
		chatLog.setAutoscrolls(true);
		scrollChatLog.setAutoscrolls(true);

		chatLog.setBackground(Color.red);

//		String startMessage = "Started chat with " + user.getUsername() + " at " + (new Date()).toString();
//		chatLog.add(new JLabel(startMessage));
		add(scrollChatLog, BorderLayout.CENTER);

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
			final JPanel messageContents = new JPanel(new BorderLayout());
			final JTextArea contents = new JTextArea(text);
			contents.setEditable(false);
			contents.setLineWrap(true);
			contents.setWrapStyleWord(true);
			contents.setAlignmentX(Component.LEFT_ALIGNMENT);
			contents.setBackground(Color.ORANGE);

			final JLabel author = new JLabel(user.getUsername());
			Font font = author.getFont();
			Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
			author.setFont(boldFont);

			final Calendar cal = Calendar.getInstance();
			final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			final JLabel time = new JLabel(sdf.format(cal.getTime()));

			// Do this before setting size..
			messageContents.add(author, BorderLayout.WEST);
			messageContents.add(contents, BorderLayout.CENTER);
			messageContents.add(time, BorderLayout.EAST);

			// int x = chatLog.getWidth();
			// int y = messageContents.getPreferredSize().height;
			// Dimension d = new Dimension(x, y);
			// messageContents.setPreferredSize(d);
			// messageContents.setMaximumSize(d);
			// messageContents.setMinimumSize(d);
			// messageContents.setBackground(Color.BLUE);
//			messageContents.setAlignmentX(Component.LEFT_ALIGNMENT);

			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			System.out.println(chatNum);
			c.gridy = chatNum++;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.weightx = 1;
			c.weighty = 0;
			
			chatLog.add(messageContents, c);
			chatLog.revalidate();
			chatLog.repaint();
		}
	}
}
