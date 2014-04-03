package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.junit.Test;

import fileTransfer.Utils;

public class TestFileUtils {

	@Test
	public void testCreateParentFolders() {
		final File a = new File("a");
		final File b = new File(a, "b");
		final File c = new File(b, "c");
		final File dFile = new File(c, "d");
		Utils.createParentFolders(dFile);

		assertTrue(a.exists());
		assertTrue(b.exists());
		assertTrue(c.exists());
		assertTrue(!dFile.exists());

		c.delete();
		b.delete();
		a.delete();
	}

	@SuppressWarnings("resource")
	@Test
	public void testFileSize() {
		final File f = new File("f");
		try {

			assertTrue(f.createNewFile());

			final OutputStream fos = new FileOutputStream(f);

			final Random random = new Random();
			final int size = random.nextInt(2000);

			for (int i = 0; i < size; i++) {
				fos.write(random.nextInt());
			}
			System.out.println(size);
			assertEquals(Utils.fileSize(f), size);

		} catch (IOException e) {
			fail(e.getMessage());
		}finally{
			f.delete();
		}
	}

}
