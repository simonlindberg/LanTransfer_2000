package fileTransfer;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

	public static void createParentFolders(final File file) {
		file.getParentFile().mkdirs();
	}

	public static long fileSize(final File file) {
		if (file.isFile()) {
			return file.length();
		} else {
			long sum = 0;
			for (final File childFile : file.listFiles()) {
				sum = sum + fileSize(childFile);
			}
			return sum;
		}
	}

	/**
	 * http://stackoverflow.com/a/5599842
	 * 
	 * @param size
	 * @return
	 */
	public static String readableFileSize(final long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
