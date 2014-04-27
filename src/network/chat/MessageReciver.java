package network.chat;

public interface MessageReciver {
	/**
	 * a new message to show to the user.
	 * 
	 * @param msg
	 */
	public void newMessage(final String msg, final int id);

	/**
	 * a confirmation that a previously sent message is recived by the other
	 * user.
	 * 
	 * @param id
	 */
	public void recivedMessage(final int id);

	/**
	 * a confirmation that a previously sent message has been seen by the user.
	 * 
	 * @param id
	 */
	public void seenMessage(final int id);
}
