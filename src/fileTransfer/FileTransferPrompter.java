package fileTransfer;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public interface FileTransferPrompter {
	public FileTransferIntermediary promptFileTransfer(final List<String> fileNames, final List<Long> fileSizes,
			final AtomicReference<String> savePlace, final CountDownLatch latch, final Socket socket);
}
