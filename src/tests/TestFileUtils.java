package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import network.fileTransfer.FileUtils;

import org.junit.Test;

public class TestFileUtils {

	@Test
	public void testCreateParentFolders() throws IOException {
		final Path a = Paths.get("a");
		final Path b = Paths.get("a", "b");
		final Path c = Paths.get("a", "b", "c");
		final Path dFile = Paths.get("a", "b", "c", "d");

		FileUtils.createParentFolders(dFile);

		assertTrue(Files.exists(a));
		assertTrue(Files.exists(b));
		assertTrue(Files.exists(c));
		assertTrue(!Files.exists(dFile));

		Files.delete(c);
		Files.delete(b);
		Files.delete(a);
	}

	@Test
	public void testFileSize() {
		final Path folder = Paths.get("lib");
		final Path file = Paths.get("lib", "miglayout-4.0.jar");

		assertEquals(203283, FileUtils.fileSize(folder));
		assertEquals(203283, FileUtils.fileSize(file));
	}

	@Test
	public void testIsFile() throws IOException {
		final File file = new File("f");
		final File folder = new File("ff");
		try {
			assertTrue(file.createNewFile());
			assertTrue(FileUtils.isFile(Paths.get("f")));
			assertTrue(folder.mkdir());
			assertFalse(FileUtils.isFile(Paths.get("ff")));
		} finally {
			file.delete();
			folder.delete();
		}
	}

	@Test
	public void testFolderContents() throws IOException {
		final String foldername = "fff";

		final File folder = new File(foldername);
		assertTrue(folder.mkdir());
		final List<Path> correct = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			new File(folder, i + "").createNewFile();
			correct.add(Paths.get(foldername, i + ""));
		}
		assertEquals(correct, FileUtils.folderContents(Paths.get(foldername)));
		for (int i = 0; i < 10; i++) {
			new File(folder, i + "").delete();
		}
		folder.delete();
	}

	@Test
	public void testShorten() {
		final StringBuffer sb = new StringBuffer();

		for (int i = 0; i < 1000; i++) {
			sb.append('a');
			assertTrue(FileUtils.shorten(sb.toString()).length() <= sb.length());
		}
	}

	@Test
	public void testReadble() {
		assertEquals("0", FileUtils.readableFileSize(-1));
		assertEquals("1 kB", FileUtils.readableFileSize(1000));
		assertEquals("1 kB/s", FileUtils.readbleTransferSpeed(1000));
	}
}
