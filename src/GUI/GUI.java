package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;

	public GUI() {
		setLayout(new BorderLayout());
		addComponents();

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents() {
		createClientTable();

		createButtons();

//		createStatusbar();
	}

	private void createStatusbar() {
		/**
		 * Statusbar
		 */
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

		statusPanel.setPreferredSize(new Dimension(getWidth(), 24));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("Everything is a-ok!");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		add(statusPanel, BorderLayout.SOUTH);
	}

	private void createButtons() {
		/**
		 * Refresh + rename
		 */
		final JComponent topPane = new JPanel(new BorderLayout());

		final JPanel renamePanel = new JPanel(new BorderLayout());
		JTextField rename = new JTextField();
		renamePanel.add(rename);

		final JPanel buttonPanel = new JPanel(new FlowLayout());
		final JButton changeName = new JButton("Change username");
		final JButton refreshButton = new JButton("Refresh list");
		buttonPanel.add(changeName);
		buttonPanel.add(refreshButton);

		topPane.add(renamePanel, BorderLayout.CENTER);
		topPane.add(buttonPanel, BorderLayout.EAST);

		add(topPane, BorderLayout.NORTH);
	}

	private void createClientTable() {
		/**
		 * Client table
		 */

		final Map<String, JFrame> clientWindows = new HashMap<String, JFrame>();

		final String[] columnNames = { "Name", "IP" };
		JTable clientTable = new JTable();
		clientTable.setDragEnabled(false);
		clientTable.setFillsViewportHeight(true);
		clientTable.getTableHeader().setReorderingAllowed(false);

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
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row == -1) return;
					// Get IP
					final String ip = (String) clientTableModel.getValueAt(row, 1);
					if (!clientWindows.containsKey(ip)) {
						ChatFrame cf = new ChatFrame(new User("firas", ip));
						clientWindows.put(ip, cf);
						System.out.println("created frame");
						cf.addWindowListener(new WindowAdapter() {

							@Override
							public void windowClosing(WindowEvent e) {
								System.out.println("hello");
								clientWindows.remove(ip);
							}

						});
					} else {
						clientWindows.get(ip).toFront();
					}
				}
			}
		});

		clientTable.setModel(clientTableModel);

		clientTableModel.addRow(new Object[] { "a", "b", "c" });

		final JScrollPane jsp = new JScrollPane(clientTable);
		add(jsp, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		new GUI();
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
