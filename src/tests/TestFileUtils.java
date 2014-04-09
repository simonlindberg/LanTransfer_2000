package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import fileTransfer.FileUtils;

public class TestFileUtils {

	@Test
	public void testCreateParentFolders() {
		final File a = new File("a");
		final File b = new File(a, "b");
		final File c = new File(b, "c");
		final File dFile = new File(c, "d");
		FileUtils.createParentFolders(dFile);

		assertTrue(a.exists());
		assertTrue(b.exists());
		assertTrue(c.exists());
		assertTrue(!dFile.exists());

		c.delete();
		b.delete();
		a.delete();
	}

	@Test
	public void testFileSize() {
		final Path folder = Paths.get("lib");
		final Path file = Paths.get("lib", "miglayout-4.0.jar");

		assertEquals(203283, FileUtils.fileSize(folder));
		assertEquals(203283, FileUtils.fileSize(file));

	}

}
