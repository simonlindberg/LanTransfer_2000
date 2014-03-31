package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import main.Main;
import main.User;

@SuppressWarnings("serial")
public class Gui extends JFrame {

	private User current = User.NULL_USER;
	private final JPanel rightContainer;
	private final Map<String, User> users;
	private JComponent introLabel;

	public Gui(final TableModel model, final Map<String, User> users, final ActionListener refresher) {
		rightContainer = new JPanel(new BorderLayout());
		this.users = users;
		setLayout(new BorderLayout());

		addComponents(model, refresher);

		setTitle("LANTRANSFER_2000 (ALPHA)");
		setSize(800, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 300));

		setVisible(true);
	}

	private void addComponents(final TableModel model, final ActionListener refresher) {
		final JComponent top = createTop(refresher);
		final JComponent clientTable = createClientTable(model);

		final JPanel leftContainer = new JPanel(new BorderLayout());
		leftContainer.add(top, BorderLayout.NORTH);
		leftContainer.add(clientTable, BorderLayout.CENTER);
		leftContainer.setPreferredSize(new Dimension(200, 200));

		introLabel = createWelcomeLabel();
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

		return introLabel;
	}

	/**
	 * Refresh + name
	 */
	private JComponent createTop(final ActionListener refresher) {
		final JComponent topPane = new JPanel(new BorderLayout());

		final JLabel nameLabel = new JLabel(Main.myUsername + " (" + Main.myIP + ")");
		final JButton refreshButton = new JButton("Refresh list");
		refreshButton.addActionListener(refresher);

		topPane.add(nameLabel, BorderLayout.WEST);
		topPane.add(refreshButton, BorderLayout.EAST);

		return topPane;
	}

	/**
	 * Client table
	 * 
	 * @param clientWindows
	 */
	private JComponent createClientTable(final TableModel model) {
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
				// Get IP
				final String ip = (String) model.getValueAt(row, 1);

				final User user = users.get(ip);
				if (user == null) {
					System.out.println("Trying to chat with unknow user.");
					return;
				}
				introLabel.setVisible(false);
				current.hideChat();
				user.showChat();
				current = user;
			}
		});
		return new JScrollPane(clientTable);
	}

	public void addChatPanel(final ChatPanel chatPanel) {
		System.out.println("add panel");
		rightContainer.add(chatPanel);
	}

	public static void showError(final String title, final String msg) {
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

}
