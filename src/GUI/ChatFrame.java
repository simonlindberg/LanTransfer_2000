package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
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

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

import net.miginfocom.swing.MigLayout;

public class ChatFrame extends JFrame {

	private static final long serialVersionUID = -2013659249126443168L;

	private User user;
	private static final Color MY_BG = new Color(244, 244, 244);
	private static final Color INFO_TXT = new Color(195, 195, 195);
	private static final Color TXT_COLOR = new Color(43, 43, 43);
	private User lastMessage;

	public ChatFrame(final User user) {
		this.user = user;
		lastMessage = null;

		setLayout(new BorderLayout());

		createComponents();

		setTitle("Chat window with " + user.getUsername());
		setSize(new Dimension(500, 600));
		// setMinimumSize(new Dimension(300, 400));
		setResizable(false);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setVisible(true);

		// TCP
		System.out.println("Connecting......!!!!!11");
	}

	private void createComponents() {
		// final JPanel chatLog = new JPanel(new GridBagLayout());
		MigLayout layout = new MigLayout("gap rel 0, wrap 1, insets 0");
		final JPanel chatLog = new JPanel(layout);
		// chatLog.setLayout(new BoxLayout(chatLog, BoxLayout.PAGE_AXIS));

		final JScrollPane scrollChatLog = new JScrollPane(chatLog);
		scrollChatLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// chatLog.setAutoscrolls(true);
		// scrollChatLog.setAutoscrolls(true);

		chatLog.setBackground(Color.WHITE);

		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
		// Space is ugly but works..
		String startMessage = " New chat started with " + user.getUsername() + " at " + sdf.format(cal.getTime());
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
					send(input.getText(), chatLog, scrollChatLog);
					input.setText("");
				}
			}
		});

		send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				send(input.getText(), chatLog, scrollChatLog);
				input.setText("");
			}
		});

		// scrollChatLog.getVerticalScrollBar().addAdjustmentListener(new
		// AdjustmentListener() {
		// public void adjustmentValueChanged(AdjustmentEvent e) {
		// e.getAdjustable().setValue(e.getAdjustable().getMaximum());
		// }
		// });
	}

	private void send(final String text, final JComponent chatLog, final JComponent scrollChatLog) {
		if (!text.equals("")) {
			System.out.println(text);
			final JPanel messageContents = new JPanel();
			messageContents.setLayout(new MigLayout("insets 0, gap rel 0", "10[]10[]10[]10", "5[]5"));
			messageContents.setBackground(MY_BG);

			final JTextArea contents = new JTextArea(text);
			contents.setEditable(false);
			contents.setLineWrap(true);
			contents.setWrapStyleWord(true);
			contents.setAlignmentX(Component.LEFT_ALIGNMENT);
			contents.setOpaque(true);
			contents.setBackground(MY_BG);

			final JLabel author = new JLabel(user.getUsername());
			Font font = author.getFont();
			Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
			author.setFont(boldFont);
			author.setForeground(INFO_TXT);

			final Calendar cal = Calendar.getInstance();
			final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			final JLabel time = new JLabel(sdf.format(cal.getTime()));
			time.setForeground(INFO_TXT);

			if (!user.equals(lastMessage)) {
				messageContents.add(author, "wrap 1");
			}
			messageContents.add(contents, "pushx, growx"); // GROW FUCKER
			messageContents.add(time);

			chatLog.add(messageContents, "pushx, growx");

			chatLog.revalidate();
			chatLog.repaint();

			// auto scroll
			int height = (int) chatLog.getPreferredSize().getHeight();
			Rectangle rect = new Rectangle(0, height, 10, 10);
			scrollChatLog.scrollRectToVisible(rect);

			lastMessage = user;
		}
	}
}
