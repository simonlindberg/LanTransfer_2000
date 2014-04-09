package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import fileTransfer.FileUtils;

public class TestFileUtils {

	@Test
	public void testCreateParentFolders() throws IOException {
		final Path a = Paths.get("a");
		final Path b = Paths.get("a", "b");
		final Path c = Paths.get("b", "c");
		final Path dFile = Paths.get("c", "d");
		FileUtils.createParentFolders(dFile);

		assertTrue(Files.exists(a));
		assertTrue(Files.exists(b));
		assertTrue(Files.exists(c));
		assertTrue(!Files.exists(dFile));

		Files.delete(a);
		Files.delete(b);
		Files.delete(c);
		Files.delete(dFile);
	}

	@Test
	public void testFileSize() {
		final Path folder = Paths.get("lib");
		final Path file = Paths.get("lib", "miglayout-4.0.jar");

		assertEquals(203283, FileUtils.fileSize(folder));
		assertEquals(203283, FileUtils.fileSize(file));

	}

}
