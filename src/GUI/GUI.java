package GUI;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.KeyEvent;

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
		// en rad, två cols
		// 10px horizontal gap, 0px vertical
		// setLayout(new GridLayout(1, 2, 10, 0));
		addComponents();

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(850, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

	}

	private void addComponents() {
		JTabbedPane tabbedPane = new JTabbedPane();

		JComponent clientListTab = makePanel(new GridLayout());
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
		

		JComponent panel2 = makePanel(new GridLayout());
		tabbedPane.addTab("Transfers", null, panel2, "See all your transfers");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setResizeWeight(0.8);
        sp.setEnabled(false);
        sp.setDividerSize(0);
		sp.add(tabbedPane);
		
		// null layout är nog det enklaste asså..
		JComponent buttonPane = makePanel(null);
		JButton testButton = new JButton("Simon");
		testButton.setBounds(20, 20, 80, 30);
		
		JButton testButton1 = new JButton("är");
		// går att göra med dimension som nedan för exakta storlekar..
//		Dimension testBtnSize1 = testButton1.getPreferredSize();
//		testButton.setBounds(20, 60, testBtnSize1.width, testBtnSize1.height);
		testButton1.setBounds(20, 60, 80, 30);
		
		JButton testButton2 = new JButton("en");
		testButton2.setBounds(20, 100, 80, 30);
		
		JButton testButton3 = new JButton("noob");
		testButton3.setBounds(20, 140, 80, 30);

		buttonPane.add(testButton);
		buttonPane.add(testButton1);
		buttonPane.add(testButton2);
		buttonPane.add(testButton3);
		
		sp.add(buttonPane);
		
		add(sp);
	}

	private JComponent makePanel(LayoutManager layout) {
		JPanel panel = new JPanel(false);
		panel.setLayout(layout);

		return panel;
	}

	public static void main(String[] args) {
		new GUI();
	}

}