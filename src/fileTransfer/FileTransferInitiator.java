package fileTransfer;

import java.net.Socket;

public interface FileTransferInitiator {

	public void initFileTransfer(final Socket socket);
}
