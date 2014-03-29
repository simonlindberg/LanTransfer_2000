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

import main.User;
import net.miginfocom.swing.MigLayout;
import chat.ChatReciver;
import chat.ChatServerThread;
import chat.Sender;

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

	private final JTextField input;
	private final JButton send = new JButton("Send");
	private final JPanel chatLog = new JPanel(new MigLayout("gap rel 0, wrap 1, insets 0"));
	private final JScrollPane scrollChatLog = new JScrollPane(chatLog);
	private Sender sender;
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

		// TCP
		System.out.println("Connecting......!!!!!11");
	}

	@Override
	public void setVisible(final boolean show) {
		super.setVisible(show);

		if (socket == null) {
			try {
				setSocket(new Socket(user.getIP(), ChatServerThread.CHAT_PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createComponents() {
		// Space is ugly but works..
		final String startMessage = " New chat started with " + user.toString() + " at "
				+ dateFormat.format(Calendar.getInstance().getTime());

		final JLabel start = new JLabel(startMessage);
		start.setForeground(INFO_TXT);

		chatLog.setForeground(TXT_COLOR);
		chatLog.setBackground(Color.WHITE);
		chatLog.add(start);

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
			System.out.println(myName);
			showMessage(myName, text);
			sendMessage(text);
			input.setText("");
		}
	}

	private void sendMessage(final String text) {
		sender.send(text);
		System.out.println("sending '" + text + "' to " + user.getUsername());
	}

	private void showMessage(final String from, final String text) {
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

	public void setOffline() {
		input.setEnabled(false);
		send.setEnabled(false);
		showMessage(user.getUsername(), "Has gone offline!");
	}

	public void showMessage(final String msg) {
		showMessage(user.getUsername(), msg);
	}

	public void setSender(final Sender sender) {
		this.sender = sender;
	}

	public void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		sender = new Sender(socket.getOutputStream());
		reciver = new ChatReciver(socket.getInputStream(), this);
		reciver.start();
	}
}
