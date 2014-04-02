package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.Main;
import net.miginfocom.swing.MigLayout;
import user.User;

@SuppressWarnings("serial")
public class ChatPanel extends JPanel {

	private static final Font BOLD = new Font(new JLabel().getFont().getFontName(), Font.BOLD, new JLabel().getFont().getSize());
	private static final Format timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Color MY_BACKGROUND = new Color(244, 244, 244);
	private static final Color INFO_TXT = new Color(195, 195, 195);
	private static final Color TXT_COLOR = new Color(43, 43, 43);
	private static final Color OTHER_BACKGROUND = Color.WHITE;
	private static final Color NOTICE_COLOR = new Color(171, 108, 108);

	private boolean lastFromMe;
	// firstMsg determines if we should show the name of the user or not. Always
	// show the name of the first message.
	private boolean firstMsg = true;

	private final User user;

	private final JTextField input;
	private final JButton send = new JButton("Send");
	private final JPanel chatLog = new JPanel(new MigLayout("gap rel 0, wrap 1, insets 0"));
	private final JScrollPane scrollChatLog = new JScrollPane(chatLog);
	
	private final JLabel sendFileImage;

	public ChatPanel(final User user) {
		this.user = user;
		input = new HintTextField(0, "Write a message to " + user.getUsername() + "!");

		setLayout(new BorderLayout());

		createComponents();

		setDropTarget(new DropTarget(this, new FileDropHandler() {

			@Override
			public void handleFiles(final List<File> files) {
				// File.isDirectory()
				System.out.println("senging files " + files);
				sendFile(false, files);
			}
		}));

		// Set scroll speed
		scrollChatLog.getVerticalScrollBar().setUnitIncrement(16);
		
		// Add upload icon
		BufferedImage uploadImage = null;
		try {
			uploadImage = ImageIO.read(new File("src/gfx/upload.png"));
		} catch (IOException e) {
			System.out.println("Unable to locate upload icon..");
			e.printStackTrace();
		}
		sendFileImage = new JLabel(new ImageIcon(uploadImage));
	}

	/**
	 * http://stackoverflow.com/a/5599842
	 * 
	 * @param size
	 * @return
	 */
	private String readableFileSize(final long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	private void sendFile(final boolean isDirectory, final List<File> files) {
		if (isDirectory) {

		} else {
			long totalSize = 0;
			for (File f : files) {
				long fileSize = f.length();
				totalSize += fileSize;

				final JPanel fileContents = new JPanel(new MigLayout("insets 0, gap rel 0", "16[]10", "[][]10"));
				fileContents.add(new JLabel(f.getName()), "wrap 1");
				fileContents.add(new JLabel(readableFileSize(fileSize)), "wrap 1");
				
				
				final JPanel messageContents = createMessagePanel(true, false, fileContents);

				addToLog(messageContents);
			}

			final JPanel submitPanel = new JPanel(new MigLayout("insets 0, gap rel 0", "[]5[]5[]2[]", "[]10[]"));
			final JButton saveAs = new JButton("Save as..");
			final JButton cancel = new JButton("Cancel");
			
			final JLabel fileInfo = new JLabel();
			fileInfo.setText(user.getUsername() + " wants to send " + files.size() + " file(s) (" + readableFileSize(totalSize) + ")");

			JProgressBar fileProgress = new JProgressBar(0, 100);
			fileProgress.setValue(20);
			fileProgress.setStringPainted(true);
			fileProgress.setPreferredSize(new Dimension(100, 5));

			submitPanel.add(sendFileImage);
			submitPanel.add(fileInfo);
			submitPanel.add(saveAs);
			submitPanel.add(cancel, "wrap");
			submitPanel.add(fileProgress, "pushx, growx, span 4");

			final JPanel submitContents = createMessagePanel(true, false, submitPanel);

			addToLog(submitContents);
		}
	}

	/**
	 * Create the initial components for each chat log. The main chatarea is
	 * created with a input and send button.
	 */
	private void createComponents() {
		chatLog.setForeground(TXT_COLOR);
		chatLog.setBackground(Color.WHITE);

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
			showMessage(Main.myUsername, text, true);
			input.setText("");
		}
	}

	private void sendMessage(final String text) {
		user.sendMessage(text);
	}

	/**
	 * Create a parent container to hold messages in the chat panel. This is
	 * used for chats AND file transfers
	 * 
	 * @param fromMe
	 * @param notice
	 * @param contents
	 *            the container to hold the actual data. Probably a textarea or
	 *            panel.
	 * @return
	 */
	private JPanel createMessagePanel(final boolean fromMe, final boolean notice, final JComponent contents) {
		final JPanel messageContents = new JPanel(new MigLayout("insets 0, gap rel 0", "10[]10[]10[]10", "5[]5"));

		if (fromMe) {
			contents.setBackground(MY_BACKGROUND);
			messageContents.setBackground(MY_BACKGROUND);
		} else {
			contents.setBackground(OTHER_BACKGROUND);
			messageContents.setBackground(OTHER_BACKGROUND);
		}

		final JLabel time = new JLabel(timeFormat.format(Calendar.getInstance().getTime()));
		time.setForeground(INFO_TXT);

		if ((fromMe != lastFromMe || firstMsg) && !notice) {
			final JLabel author = new JLabel(fromMe ? Main.myUsername : user.getUsername());
			author.setForeground(INFO_TXT);
			author.setFont(BOLD);
			messageContents.add(author, "wrap 1, gapy 0 10");
			lastFromMe = fromMe;
		}

		// Width required for bug with textarea and linewrap.
		messageContents.add(contents, "width 10:50:, pushx, growx");
		messageContents.add(time);

		return messageContents;
	}

	/**
	 * Show a text message received through chat
	 * 
	 * @param text
	 * @param color
	 * @param fromMe
	 * @param notice
	 */
	private void showMessage(final String text, final Color color, final boolean fromMe, final boolean notice) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final JTextArea contents = new JTextArea(text);
				contents.setEditable(false);
				contents.setLineWrap(true);
				contents.setWrapStyleWord(true);
				contents.setAlignmentX(Component.LEFT_ALIGNMENT);
				contents.setOpaque(true);
				contents.setForeground(color);

				final JPanel messageContents = createMessagePanel(fromMe, notice, contents);

				addToLog(messageContents);
			}

		});
	}

	/**
	 * Shorthand to add new container to the chat log and automatically scroll
	 * to the bottom.
	 * 
	 * @param messageContents
	 */
	private void addToLog(final JPanel messageContents) {
		chatLog.add(messageContents, "pushx, growx");

		chatLog.revalidate();
		chatLog.repaint();

		// auto scroll
		// Note that messageContents.getPref() might be unnecessary!
		final int height = (int) (chatLog.getPreferredSize().getHeight() + messageContents.getPreferredSize().getHeight());
		Rectangle rect = new Rectangle(0, height, 10, 10);
		scrollChatLog.scrollRectToVisible(rect);

		firstMsg = false;
	}

	public void showMessage(final String msg) {
		showMessage(user.getUsername(), msg, false);
	}

	private void showMessage(final String username, final String message, final boolean fromMe) {
		showMessage(message, TXT_COLOR, fromMe, false);
	}

	private void showNotice(final String text) {
		showMessage(text, NOTICE_COLOR, false, true);
	}

	public void setOnline() {
		input.setEnabled(true);
		send.setEnabled(true);
		showNotice(user.getUsername() + " is online!");
	}

	public void setOffline() {
		input.setEnabled(false);
		send.setEnabled(false);
		showNotice(user.getUsername() + " has gone offline!");
	}
}
