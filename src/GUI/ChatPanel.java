package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.User;
import net.miginfocom.swing.MigLayout;
import chat.ChatReciver;
import chat.ChatSender;
import chat.ChatServerThread;

@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

	private static final Font BOLD = new Font(new JLabel().getFont().getFontName(), Font.BOLD, new JLabel().getFont().getSize());
	private static final Format timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Color MY_BACKGROUND = new Color(244, 244, 244);
	private static final Color INFO_TXT = new Color(195, 195, 195);
	private static final Color TXT_COLOR = new Color(43, 43, 43);
	private static final Color OTHER_BACKGROUND = Color.WHITE;
	private static final Color NOTICE_COLOR = new Color(171, 108, 108);

	private String lastSender;

	private final User user;
	private final String myName;

	private final JTextField input;
	private final JButton send = new JButton("Send");
	private final JPanel chatLog = new JPanel(new MigLayout("gap rel 0, wrap 1, insets 0"));
	private final JScrollPane scrollChatLog = new JScrollPane(chatLog);

	private ChatSender sender;
	private Socket socket;
	private ChatReciver reciver;

	public ChatPanel(final String name, final User user) {
		this.user = user;
		this.myName = name;
		lastSender = null;
		input = new HintTextField(0, "Write a message to " + user.getUsername() + "!");

		setLayout(new BorderLayout());

		createComponents();

		setDropTarget(new DropTarget(this, new FileDropHandle() {

			@Override
			public void handleFiles(final List<File> files) {
				System.out.println("senging files " + files);
			}
		}));

		// Set scroll speed
		scrollChatLog.getVerticalScrollBar().setUnitIncrement(16);
	}

	@Override
	public void setVisible(final boolean show) {
		super.setVisible(show);

		if (socket == null) {
			try {
				setSocket(new Socket(user.getIP(), ChatServerThread.CHAT_PORT));
			} catch (IOException e) {
				e.printStackTrace();
				// om man försöker chatta med nån som nyss gick offline
			}
		}
	}

	private void createComponents() {
		// Space is ugly but works..
		final String startMessage = "New chat started with " + user.toString();

		// final JLabel start = new JLabel(startMessage);
		// start.setForeground(INFO_TXT);
		showNotice(startMessage);

		chatLog.setForeground(TXT_COLOR);
		chatLog.setBackground(Color.WHITE);
		// chatLog.add(start);

		input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleMessage();
				}
			}

		});

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleMessage();
			}
		});

		final JPanel chatInput = new JPanel(new BorderLayout());
		chatInput.add(input, BorderLayout.CENTER);
		chatInput.add(send, BorderLayout.EAST);

		add(scrollChatLog, BorderLayout.CENTER);
		add(chatInput, BorderLayout.SOUTH);
	}

	private void handleMessage() {
		final String text = input.getText();
		if (!text.equals("")) {
			sendMessage(text);
			showMessage(myName, text);
			input.setText("");
		}
	}

	private void sendMessage(final String text) {
		sender.send(text);
	}

	private void showMessage(final String from, final String text, final Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final JLabel author = new JLabel(from);
				author.setForeground(INFO_TXT);
				author.setFont(BOLD);

				final JTextArea contents = new JTextArea(text);
				contents.setEditable(false);
				contents.setLineWrap(true);
				contents.setWrapStyleWord(true);
				contents.setAlignmentX(Component.LEFT_ALIGNMENT);
				contents.setOpaque(true);
				contents.setForeground(color);

				final JPanel messageContents = new JPanel();
				messageContents.setLayout(new MigLayout("insets 0, gap rel 0", "10[]10[]10[]10", "5[]5"));

				if (from.equals(myName)) {
					contents.setBackground(MY_BACKGROUND);
					messageContents.setBackground(MY_BACKGROUND);
				} else {
					contents.setBackground(OTHER_BACKGROUND);
					messageContents.setBackground(OTHER_BACKGROUND);
				}

				final JLabel time = new JLabel(timeFormat.format(Calendar.getInstance().getTime()));
				time.setForeground(INFO_TXT);

				if (!from.equals(lastSender)) {
					messageContents.add(author, "wrap 1, gapy 0 10");
					lastSender = from;
				}

				messageContents.add(contents, "width 10:50:, pushx, growx");
				messageContents.add(time);

				chatLog.add(messageContents, "pushx, growx");

				chatLog.revalidate();
				chatLog.repaint();

				// auto scroll
				final int height = (int) (chatLog.getPreferredSize().getHeight() + messageContents.getPreferredSize().getHeight());
				Rectangle rect = new Rectangle(0, height, 10, 10);
				scrollChatLog.scrollRectToVisible(rect);
			}
		});
	}

	public void setOffline() {
		input.setEnabled(false);
		send.setEnabled(false);
		showNotice(user.getUsername() + " has gone offline!");
	}

	public void showMessage(final String msg) {
		showMessage(user.getUsername(), msg);
	}

	public void setSender(final ChatSender sender) {
		this.sender = sender;
	}

	public void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		sender = new ChatSender(socket.getOutputStream());
		reciver = new ChatReciver(socket.getInputStream(), this);
		reciver.start();
	}

	private void showMessage(String username, String message) {
		showMessage(username, message, TXT_COLOR);
	}
	
	private void showNotice(String text) {
		showMessage("", text, NOTICE_COLOR);
	}
}
