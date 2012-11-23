package net.sf.iwant.entry3;

import java.io.File;

import junit.framework.TestCase;

public class FileUtilTest extends TestCase {

	public void testRelativePathFailsWithDifferentParent() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b is not a child of /a", e.getMessage());
		}
	}

	public void testRelativePathFailsWithFileUnderDifferentParent() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b/c"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b/c is not a child of /a", e.getMessage());
		}
	}

	public void testRelativePathFailsWithRoot() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/ is not a child of /a", e.getMessage());
		}
	}

	public void testRelativePathFailsWithNull() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(null, parent);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	public void testRelativePathIsEmptyForParentItself() {
		File parent = new File("/a");
		assertEquals("",
				FileUtil.relativePathOfFileUnderParent(new File("/a"), parent));
	}

	public void testRelativePathIsEmptyForRoot() {
		File parent = new File("/");
		assertEquals("",
				FileUtil.relativePathOfFileUnderParent(new File("/"), parent));
	}

	public void testRelativePathIsBForAB() {
		File parent = new File("/a");
		assertEquals("b", FileUtil.relativePathOfFileUnderParent(new File(
				"/a/b"), parent));
	}

	public void testRelativePathIsBCForABC() {
		File parent = new File("/a");
		assertEquals("b/c", FileUtil.relativePathOfFileUnderParent(new File(
				"/a/b/c"), parent));
	}

}
