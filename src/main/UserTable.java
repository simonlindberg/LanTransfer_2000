package main;

import javax.swing.table.TableModel;

public interface UserTable extends TableModel {

	public void removeUser(User user);

	public void addUser(User user);

	public void clear();
}
