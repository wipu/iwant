package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {

	public void testRelativePathFailsWithDifferentParent() throws IOException {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b is not a child of /a", e.getMessage());
		}
	}

	public void testRelativePathFailsWithFileUnderDifferentParent()
			throws IOException {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b/c"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b/c is not a child of /a", e.getMessage());
		}
	}

	public void testRelativePathIsEmptyForParent() throws IOException {
		File parent = new File("/a");
		assertEquals("",
				FileUtil.relativePathOfFileUnderParent(new File("/a"), parent));
	}

	public void testRelativePathIsBForAB() throws IOException {
		File parent = new File("/a");
		assertEquals("b", FileUtil.relativePathOfFileUnderParent(new File(
				"/a/b"), parent));
	}

}
