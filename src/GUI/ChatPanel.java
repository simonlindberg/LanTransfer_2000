package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

	private static final Font BOLD = new Font(new JLabel().getFont().getFontName(), Font.BOLD, new JLabel().getFont().getSize());
	private static final Format dateFormat = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
	private static final Format timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Color MY_BACKGROUND = new Color(244, 244, 244);
	private static final Color INFO_TXT = new Color(195, 195, 195);
	private static final Color TXT_COLOR = new Color(43, 43, 43);

	private String lastSender;

	private final User user;
	private final String myName;

	public ChatPanel(final String name, final User user) {
		this.user = user;
		this.myName = name;
		lastSender = null;

		setLayout(new BorderLayout());

		createComponents();

		// TCP
		System.out.println("Connecting......!!!!!11");
	}

	private void createComponents() {
		final JPanel chatLog = new JPanel(new MigLayout("gap rel 0, wrap 1, insets 0"));
		chatLog.setForeground(TXT_COLOR);
		chatLog.setBackground(Color.WHITE);

		final JScrollPane scrollChatLog = new JScrollPane(chatLog);

		final Calendar cal = Calendar.getInstance();

		// Space is ugly but works..
		final String startMessage = " New chat started with " + user.toString() + " at " + dateFormat.format(cal.getTime());
		final JLabel start = new JLabel(startMessage);
		start.setForeground(INFO_TXT);

		chatLog.add(start);
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
					handleMessage(chatLog, scrollChatLog, input);
				}
			}

		});

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleMessage(chatLog, scrollChatLog, input);
			}
		});
	}

	private void handleMessage(final JPanel chatLog, final JScrollPane scrollChatLog, final JTextField input) {
		final String text = input.getText();
		if (!text.equals("")) {
			showMessage(myName, text, chatLog, scrollChatLog);
			sendMessage(text);
			input.setText("");
		}
	}

	private void sendMessage(final String text) {
		System.out.println("sending '" + text + "' to " + user.getUsername());
	}

	private void showMessage(final String from, final String text, final JComponent chatLog, final JComponent scrollChatLog) {
		final JLabel author = new JLabel(from);
		author.setForeground(INFO_TXT);
		author.setFont(BOLD);

		final JTextArea contents = new JTextArea(text);
		contents.setEditable(false);
		contents.setLineWrap(true);
		contents.setWrapStyleWord(true);
		contents.setAlignmentX(Component.LEFT_ALIGNMENT);
		contents.setOpaque(true);
		contents.setBackground(MY_BACKGROUND);

		final JLabel time = new JLabel(timeFormat.format(Calendar.getInstance().getTime()));
		time.setForeground(INFO_TXT);

		final JPanel messageContents = new JPanel();
		messageContents.setLayout(new MigLayout("insets 0, gap rel 0", "10[]10[]10[]10", "5[]5"));
		messageContents.setBackground(MY_BACKGROUND);

		if (!from.equals(lastSender)) {
			messageContents.add(author, "wrap 1, gapy 0 10");
			lastSender = from;
		}

		messageContents.add(contents, "width 10:50:, pushx, growx");
		messageContents.add(time);

		chatLog.add(messageContents, "pushx, growx");

		// auto scroll
		final int height = (int) chatLog.getPreferredSize().getHeight();
		Rectangle rect = new Rectangle(0, height, 10, 10);
		scrollChatLog.scrollRectToVisible(rect);
	}
}
