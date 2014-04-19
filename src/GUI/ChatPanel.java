package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import main.Main;
import net.miginfocom.swing.MigLayout;
import network.fileTransfer.FileTransferIntermediary;
import network.fileTransfer.FileTransferSender;
import network.fileTransfer.FileUtils;
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
	private boolean forceName = true;

	private final User user;

	private final JTextField input;
	private final JButton send = new JButton("Send");
	private final JPanel chatLog = new JPanel(new MigLayout("gap rel 0, wrap 1, insets 0"));
	private final JScrollPane scrollChatLog = new JScrollPane(chatLog);

	public ChatPanel(final User user) {
		this.user = user;
		input = new HintTextField(0, "Write a message to " + user.getUsername() + "!");

		setLayout(new BorderLayout());

		createComponents();

		setDropTarget(new DropTarget(this, new FileDropHandler() {

			@Override
			public void handleFiles(final List<Path> files) {
				sendFiles(files);
			}
		}));

		// Set scroll speed
		scrollChatLog.getVerticalScrollBar().setUnitIncrement(16);
	}

	public FileTransferIntermediary promptFileTransfer(final List<String> fileNames, final List<Long> fileSizes,
			final AtomicReference<String> savePlace, final CountDownLatch latch, final Socket socket) {
		// Sätter detta så namnet syns nästa gång man skriver
		forceName = true;
		long totalSize = 0;
		for (int i = 0; i < fileSizes.size(); i++) {
			totalSize += fileSizes.get(i);

			createFilePanel(fileSizes.get(i), fileNames.get(i), false);
		}
		final JButton saveAs = new JButton("Save ass..");
		saveAs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				final int returnValue = chooser.showSaveDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					final File saveFile = chooser.getSelectedFile();
					System.out.println("CHOSEN!: " + saveFile);
					savePlace.set(saveFile.getAbsolutePath());
					latch.countDown();
					saveAs.setVisible(false);
				}
			}
		});
		final FileTransferIntermediary intermediary = createTransferPanel(false, fileSizes.size(), totalSize, saveAs, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				latch.countDown();
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		return intermediary;

	}

	private void createFilePanel(final long fileSize, final String fileName, final boolean fromMe) {
		final JPanel fileContents = new JPanel(new MigLayout("insets 0, gap rel 0", "16[]10", "[][]5"));
		final JLabel nameLabel = new JLabel(fileName);
		nameLabel.setFont(BOLD);
		fileContents.add(nameLabel, "wrap 1");
		fileContents.add(new JLabel(FileUtils.readableFileSize(fileSize)), "wrap 1");

		final JPanel messageContents = createMessagePanel(fromMe, false, fileContents, false);

		addToLog(messageContents);
	}

	private FileTransferIntermediary createTransferPanel(final boolean fromMe, final int numOfFiles, final long totalSize,
			final JButton saveAs, final ActionListener cancelAction) {

		final JPanel submitPanel = new JPanel(new MigLayout("insets 0, gap rel 0", "[][][]", "[]10[]"));

		final JLabel fileInfo = new JLabel();
		fileInfo.setText((fromMe ? "You want" : user.getUsername() + " wants") + " to send " + numOfFiles + " file(s) ("
				+ FileUtils.readableFileSize(totalSize) + ")");

		final JProgressBar fileProgress = new JProgressBar(0, 100);
		fileProgress.setValue(0);
		fileProgress.setStringPainted(true);
		fileProgress.setString("waiting for response...");

		submitPanel.add(fileInfo);

		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(cancelAction);
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (saveAs != null) {
					saveAs.setVisible(false);
				}
				cancel.setVisible(false);
				System.out.println("transfer cancelled!");
			}
		});
		if (saveAs != null) {
			submitPanel.add(saveAs);
		}
		submitPanel.add(cancel, "wrap");
		submitPanel.add(fileProgress, "pushx, growx, spanx 4, height 5");

		final JPanel submitContents = createMessagePanel(fromMe, false, submitPanel, true);

		addToLog(submitContents);

		// Sätter detta så namnet syns nästa gång man skriver
		forceName = true;

		return new Intermediary(cancel, saveAs, fileProgress);
	}

	private void sendFiles(final List<Path> filePaths) {
		if (!user.isOnline()) {
			return;
		}
		// Sätter detta så namnet syns nästa gång man skriver
		forceName = true;
		System.out.println("sending files: " + filePaths);
		long totalSize = 0;
		for (final Path path : filePaths) {
			final long fileSize = FileUtils.fileSize(path);
			totalSize += fileSize;

			createFilePanel(fileSize, path.getFileName().toString(), true);
		}

		final Socket socket = new Socket();
		final FileTransferIntermediary intermediary = createTransferPanel(true, filePaths.size(), totalSize, null, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		new FileTransferSender(filePaths, user.getIP(), intermediary, socket).start();
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
	private JPanel createMessagePanel(final boolean fromMe, final boolean notice, final JComponent contents, boolean displayTime) {
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

		if ((fromMe != lastFromMe || forceName) && !notice) {
			final JLabel author = new JLabel(fromMe ? Main.myUsername : user.getUsername());
			author.setForeground(INFO_TXT);
			author.setFont(BOLD);
			messageContents.add(author, "wrap 1, gapy 0 10");
			lastFromMe = fromMe;
			forceName = false;
		}

		// Width required for bug with textarea and linewrap.
		messageContents.add(contents, "width 10:50:, pushx, growx");
		if (displayTime) {
			messageContents.add(time);
		}

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

				contents.setDropTarget(new DropTarget(contents, new FileDropHandler() {

					@Override
					public void handleFiles(List<Path> files) {
						sendFiles(files);
					}
				}));

				final JPanel messageContents = createMessagePanel(fromMe, notice, contents, true);

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
	}

	public void showMessage(final String msg) {
		showMessage(user.getUsername(), msg, false);
	}

	private void showMessage(final String username, final String message, final boolean fromMe) {
		showMessage(message, TXT_COLOR, fromMe, false);
	}

	private void showNotice(final String text) {
		// Sätter detta så namnet syns nästa gång man skriver
		forceName = true;
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