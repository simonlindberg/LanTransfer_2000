package network;

public class NetworkUtils {
	public static final int NOTHING = 0;
	public static final int ACCEPT = 1;
	public static final int CANCEL = 2;

	public static final int CHAT_PORT = 8888;
	public static final int FILETRANSFER_PORT = 10001;
	public static final int BROADCAST_PORT = 31173;

	public static void startChatServer(final Initiator initiator) {
		new ServerThread(CHAT_PORT, initiator).start();
	}

	public static void startFileTransferServer(final Initiator initiator) {
		new ServerThread(FILETRANSFER_PORT, initiator).start();
	}
}
