package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.SocketException;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import broadcast.Broadcast;
import broadcast.BroadcastSender;
import broadcast.User;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;
	private static JTable clientTable;

	public GUI() {
		addComponents();

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(700, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents() {
		final JTabbedPane tabbedPane = new JTabbedPane();

		final JComponent clientListTab = new JPanel(new GridLayout());
		tabbedPane.addTab("Client list", null, clientListTab,
				"See all available clients");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		// add table
		final String[] columnNames = { "Name", "IP", "Port" };
		clientTable = new JTable();
		clientTable.setDragEnabled(false);
		clientTable.setFillsViewportHeight(true);
		clientTable.getTableHeader().setReorderingAllowed(false);

		DefaultTableModel clientTableModel = new DefaultTableModel(null,
				columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		clientTable.setModel(clientTableModel);

		final JScrollPane jsp = new JScrollPane(clientTable);
		clientListTab.add(jsp);

		final JComponent panel2 = new JPanel(new GridLayout());
		tabbedPane.addTab("Transfers", null, panel2, "See all your transfers");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setResizeWeight(0.8);
		sp.setEnabled(false);
		sp.setDividerSize(0);
		sp.add(tabbedPane);

		final JComponent buttonPane = new JPanel(null);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));

		final JButton refreshButton = new JButton("Refresh list");
		final JButton sendFile = new JButton("Send file (test)");
		final JButton testButton2 = new JButton("en");
		final JButton testButton3 = new JButton("noob");

		buttonPane.add(Box.createRigidArea(new Dimension(20, 20)));
		buttonPane.add(refreshButton);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(sendFile);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(testButton2);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(testButton3);

		sp.add(buttonPane);

		add(sp);

		// create the status bar panel and shove it down the bottom of the frame
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

		statusPanel.setPreferredSize(new Dimension(getWidth(), 24));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("Everything is a-ok!");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		add(statusPanel, BorderLayout.SOUTH);

		// Actionlisteners
		refreshButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				statusLabel.setText("Refreshing client list...");
				BroadcastSender.forceBroadcast();
				statusLabel.setText("Done!");
			}
		});
		
		sendFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				BroadcastSender.sendTestFile();
			}
		});
	}

	public static void main(String[] args) {
		new GUI();
		
		try {
			Broadcast.start();
			BroadcastSender.forceBroadcast();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showError(
					"An error occured while trying to broadcast. The program will now terminate.",
					"ERROR");
			System.exit(1);
		}
	}

	public static void showError(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	public static void populateGUI(Set<User> users) {
		DefaultTableModel model = (DefaultTableModel) clientTable.getModel();
	    model.setRowCount(0);
	    for (User u : users) {
	    	model.addRow(new Object[]{ u.getUsername(), u.getIP(), u.getPort() });
	    }
	}
	
}
