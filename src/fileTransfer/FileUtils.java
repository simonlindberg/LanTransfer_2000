package fileTransfer;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

	public static void createParentFolders(final File file) {
		file.getParentFile().mkdirs();
	}

	public static boolean isFile(final File f) {
		return f.isFile() || !f.isDirectory();
	}

	public static long fileSize(final File file) {
		if (isFile(file)) {
			return file.length();
		} else {
			long sum = 0;
			for (final File childFile : file.listFiles()) {
				sum = sum + fileSize(childFile);
			}
			return sum;
		}
	}

	public static String readableFileSize(final long size) {
		return readable(size, unitsSize);
	}

	public static String readbleTransferSpeed(final long speed) {
		return readable(speed, unitsSpeed);
	}

	private static final String[] unitsSize = new String[] { "B", "kB", "MB", "GB", "TB" };
	private static final String[] unitsSpeed = new String[] { "B/s", "kB/s", "MB/s", "GB/s", "TB/s" };

	/**
	 * http://stackoverflow.com/a/5599842
	 * 
	 * @param size
	 * @return
	 */
	private static String readable(final long val, final String[] units) {
		if (val <= 0) {
			return "0";
		}
		final int digitGroups = (int) (Math.log10(val) / Math.log10(1000));
		return new DecimalFormat("#,##0.#").format(val / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
	}

	public static String shorten(final String filename) {
		if (filename.length() < 40) {
			return filename;
		}
		return filename.substring(0, 18) + "..." + filename.substring(filename.length() - 18, filename.length() - 1);
	}
}
