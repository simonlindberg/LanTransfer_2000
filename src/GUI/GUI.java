package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

@SuppressWarnings("serial")
public class GUI extends JFrame {

	private final Map<String, JComponent> clientWindows;

	private JComponent currentChat;

	public GUI(final String name, final String ip) {
		setLayout(new BorderLayout());
		clientWindows = new HashMap<String, JComponent>();

		addComponents(name, ip);

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents(final String name, final String ip) {
		final JPanel leftContainer = new JPanel(new BorderLayout());
		final JPanel rightContainer = new JPanel(new BorderLayout());

		final JComponent top = createTop(name, ip);
		final JComponent clientTable = createClientTable(rightContainer);
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
	private JComponent createTop(final String name, final String ip) {
		final JComponent topPane = new JPanel(new BorderLayout());

		final JLabel nameLabel = new JLabel(name + " (" + ip + ")");
		final JButton refreshButton = new JButton("Refresh list");

		topPane.add(nameLabel, BorderLayout.WEST);
		topPane.add(refreshButton, BorderLayout.EAST);

		return topPane;
	}

	/**
	 * Client table
	 */
	private JComponent createClientTable(final JComponent rightContainer) {
		final String[] columnNames = { "Name", "IP" };
		JTable clientTable = new JTable();
		clientTable.setDragEnabled(false);
		clientTable.setFillsViewportHeight(true);
		clientTable.getTableHeader().setReorderingAllowed(false);
		clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		Object[][] data = { { "Blah", "Blah" } };

		final DefaultTableModel clientTableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}

		};

		clientTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				final JTable target = (JTable) e.getSource();
				final int row = target.getSelectedRow();
				if (row == -1) {
					return;
				}
				// Get IP
				final String ip = (String) clientTableModel.getValueAt(row, 1);
				final String username = (String) clientTableModel.getValueAt(row, 0);

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

		clientTable.setModel(clientTableModel);

		clientTableModel.addRow(new Object[] { "a", "b" });

		final JScrollPane scrollPane = new JScrollPane(clientTable);

		return scrollPane;
	}

	private void switchPanelTo(final JComponent cf) {
		currentChat.setVisible(false);
		currentChat = cf;
		currentChat.setVisible(true);
	}

}
