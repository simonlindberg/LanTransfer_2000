package GUI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;

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
		final Object[][] data = {
				{ "FIRAS", "192.168.0.1", "1337" }
		};
		final JTable clientTable = new JTable();
		clientTable.setDragEnabled(false);
		clientTable.setFillsViewportHeight(true);
		clientTable.getTableHeader().setReorderingAllowed(false);

		DefaultTableModel clientTableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
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
		
		final JButton testButton = new JButton("Simon");
		final JButton testButton1 = new JButton("är");
		final JButton testButton2 = new JButton("en");
		final JButton testButton3 = new JButton("noob");

		buttonPane.add(Box.createRigidArea(new Dimension(20, 20)));
		buttonPane.add(testButton);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(testButton1);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(testButton2);
		buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPane.add(testButton3);
		
		sp.add(buttonPane);
		
		add(sp);
	}

	public static void main(String[] args) {
		new GUI();
	}

}