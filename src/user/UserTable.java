package user;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public interface UserTable extends TableModel, TableCellRenderer {

	public void removeUser(User user);

	public void addUser(User user);

	public void clear();
	
	public void updateUser(User user);
}
