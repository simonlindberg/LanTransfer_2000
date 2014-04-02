package fileTransfer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTransfer {

	public static void main(final String[] a) throws IOException {
		new FileTransferServer().start();

		List<File> files = new ArrayList<File>();
		files.add(new File("lib"));

		new FileTransferSender(files, "127.0.0.1").start();
	}

}
