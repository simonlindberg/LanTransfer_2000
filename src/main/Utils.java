package main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Utils {

	public static void createParentFolders(final Path currentFile) {
		try {
			Files.createDirectories(currentFile.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isFile(final Path file) {
		return !Files.isDirectory(file);
	}

	public static long fileSize(final Path path) {
		final AtomicLong val = new AtomicLong(0);
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					val.addAndGet(Files.size(file));
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return val.get();
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

	public static List<Path> folderContents(final Path directory) {
		final List<Path> fileNames = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			for (Path path : directoryStream) {
				fileNames.add(path);
			}
		} catch (IOException ex) {
		}
		return fileNames;
	}

	private static final String os = System.getProperty("os.name").toLowerCase();

	public static boolean isMac() {
		return os.contains("mac");
	}

	public static boolean isWindows() {
		return os.contains("win");
	}
	
	public static boolean isLinux() {
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0);
	}

}
