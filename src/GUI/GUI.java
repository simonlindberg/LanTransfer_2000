package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import main.User;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private final Map<String, JComponent> clientWindows = new HashMap<String, JComponent>();

	private JComponent currentChat;

	public GUI(final String name, final String ip, final DefaultTableModel model, final ActionListener refresher) {
		setLayout(new BorderLayout());

		addComponents(name, ip, model, refresher);

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents(final String name, final String ip, final DefaultTableModel model, final ActionListener refresher) {
		final JPanel leftContainer = new JPanel(new BorderLayout());
		final JPanel rightContainer = new JPanel(new BorderLayout());

		final JComponent top = createTop(name, ip, refresher);
		final JComponent clientTable = createClientTable(rightContainer, model);
		final JComponent introLabel = createWelcomeLabel();

		leftContainer.add(top, BorderLayout.NORTH);
		leftContainer.add(clientTable, BorderLayout.CENTER);
		leftContainer.setPreferredSize(new Dimension(200, 200));
		rightContainer.add(introLabel, BorderLayout.CENTER);
		final JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, rightContainer);
		jsp.setDividerSize(5);
		jsp.setResizeWeight(0.1);

		add(jsp);
	}

	private JComponent createWelcomeLabel() {
		final String welcomeMessage = "Welcome to LANMASTER_2000!";
		final JLabel introLabel = new JLabel(welcomeMessage);

		introLabel.setHorizontalAlignment(SwingConstants.CENTER);

		currentChat = introLabel;

		return introLabel;
	}

	/**
	 * Refresh + name
	 */
	private JComponent createTop(final String name, final String ip, final ActionListener refresher) {
		final JComponent topPane = new JPanel(new BorderLayout());

		final JLabel nameLabel = new JLabel(name + " (" + ip + ")");
		final JButton refreshButton = new JButton("Refresh list");
		refreshButton.addActionListener(refresher);

		topPane.add(nameLabel, BorderLayout.WEST);
		topPane.add(refreshButton, BorderLayout.EAST);

		return topPane;
	}

	/**
	 * Client table
	 */
	private JComponent createClientTable(final JComponent rightContainer, final DefaultTableModel model) {
		final JTable clientTable = new JTable();
		clientTable.setDragEnabled(false);
		clientTable.setModel(model);
		clientTable.setFillsViewportHeight(true);
		clientTable.getTableHeader().setReorderingAllowed(false);
		clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		clientTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent e) {
				final JTable target = (JTable) e.getSource();
				final int row = target.getSelectedRow();
				if (row == -1) {
					return;
				}
				// Get IP & username
				final String ip = (String) model.getValueAt(row, 1);
				final String username = (String) model.getValueAt(row, 0);
				if (!clientWindows.containsKey(ip)) {
					ChatPanel newChat = new ChatPanel(username, new User(username, ip));
					clientWindows.put(ip, newChat);
					rightContainer.add(newChat);
					System.out.println("created panel");
				}
				final JComponent cf = clientWindows.get(ip);
				switchPanelTo(cf);
			}
		});
		return new JScrollPane(clientTable);
	}

	private void switchPanelTo(final JComponent cf) {
		currentChat.setVisible(false);
		currentChat = cf;
		currentChat.setVisible(true);
	}

}
