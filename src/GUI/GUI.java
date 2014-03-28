package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
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

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private Map<String, JComponent> clientWindows;
	private JPanel leftContainer;
	private JPanel rightContainer;

	public GUI(final String name) {
		setLayout(new BorderLayout());
		leftContainer = new JPanel(new BorderLayout());
		rightContainer = new JPanel(new BorderLayout());

		addComponents(name);

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents(final String name) {
		createClientTable(leftContainer);
		createButtons(leftContainer);

		leftContainer.setPreferredSize(new Dimension(200, 200));

		// final ChatPanel cp = new ChatPanel(name, new User("firas",
		// "192.168.0.1"));
		//
		// rightContainer.add(cp);
		final String introMessage = "Welcome to LANMASTER_2000!";
		final JLabel intro = new JLabel(introMessage);
		intro.setHorizontalAlignment(SwingConstants.CENTER);
		rightContainer.add(intro, BorderLayout.CENTER);

		final JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, rightContainer);

		jsp.setDividerSize(5);
		jsp.setResizeWeight(0.1);

		add(jsp);
	}

	private void createButtons(final JComponent container) {
		/**
		 * Refresh + rename
		 */
		final JComponent topPane = new JPanel(new BorderLayout());

		final JButton changeName = new JButton("Change username");
		final JButton refreshButton = new JButton("Refresh list");

		topPane.add(changeName, BorderLayout.WEST);
		topPane.add(refreshButton, BorderLayout.EAST);

		container.add(topPane, BorderLayout.NORTH);
	}

	private void createClientTable(final JComponent container) {
		/**
		 * Client table
		 */

		clientWindows = new HashMap<String, JComponent>();

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
				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				if (row == -1) {
					return;
				}
				// Get IP
				final String ip = (String) clientTableModel.getValueAt(row, 1);
				final String username = (String) clientTableModel.getValueAt(row, 0);
				ChatPanel cf = null;
				if (!clientWindows.containsKey(ip)) {
					cf = new ChatPanel(username, new User(username, ip));
					clientWindows.put(ip, cf);
				} else {
					cf = (ChatPanel) clientWindows.get(ip);
				}
				switchPanelTo(cf);
				System.out.println("created panel");
			}

		});

		clientTable.setModel(clientTableModel);

		clientTableModel.addRow(new Object[] { "a", "b", "c" });

		final JScrollPane scrollPane = new JScrollPane(clientTable);
		container.add(scrollPane, BorderLayout.CENTER);
	}

	private void switchPanelTo(ChatPanel cf) {
		for (Component j : rightContainer.getComponents()) {
			j.setVisible(false);
		}
		// Is this bad? Adding it several times?
		rightContainer.add(cf, BorderLayout.CENTER);
		cf.setVisible(true);
	}
	// public static void showError(String title, String message) {
	// JOptionPane.showMessageDialog(null, message, title,
	// JOptionPane.ERROR_MESSAGE);
	// }
	//
	// public static void clearGUI() {
	// DefaultTableModel model = (DefaultTableModel) clientTable.getModel();
	// // model.setRowCount(0);
	// }
	//
	// public static void populateGUI(Set<User> users) {
	// clearGUI();
	// DefaultTableModel model = (DefaultTableModel) clientTable.getModel();
	// for (User u : users) {
	// model.addRow(new Object[] { u.getUsername(), u.getIP() });
	// }
	// }

}
