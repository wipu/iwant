package net.sf.iwant.coreservices;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

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
		assertEquals("b", FileUtil
				.relativePathOfFileUnderParent(new File("/a/b"), parent));
	}

	public void testRelativePathIsBCForABC() {
		File parent = new File("/a");
		assertEquals("b/c", FileUtil
				.relativePathOfFileUnderParent(new File("/a/b/c"), parent));
	}

	public void testCopyMissingFilesExcludesSvnMetafiles() throws IOException {
		TestArea testArea = TestArea.forTest(this);
		File from = testArea.newDir("from");
		testArea.newDir("from/.svn");
		testArea.hasFile("from/A", "");
		testArea.newDir("from/b");
		testArea.newDir("from/b/.svn");
		testArea.hasFile("from/b/B", "");

		File to = testArea.newDir("to");

		FileUtil.copyMissingFiles(from, to);

		assertFalse(new File(to, ".svn").exists());
		assertTrue(new File(to, "A").exists());
		assertFalse(new File(to, "b/.svn").exists());
		assertTrue(new File(to, "b/B").exists());
	}

	public void testCopyRecursivelyIncludesSvnMetafilesIfToldSo()
			throws IOException {
		TestArea testArea = TestArea.forTest(this);
		File from = testArea.newDir("from");
		testArea.newDir("from/.svn");
		testArea.hasFile("from/A", "");
		testArea.newDir("from/b");
		testArea.newDir("from/b/.svn");
		testArea.hasFile("from/b/B", "");

		File to = testArea.newDir("to");

		FileUtil.copyRecursively(from, to, true);

		assertTrue(new File(to, ".svn").exists());
		assertTrue(new File(to, "A").exists());
		assertTrue(new File(to, "b/.svn").exists());
		assertTrue(new File(to, "b/B").exists());
	}

	public void testCopyRecursivelyPreservedXFlag() throws IOException {
		TestArea testArea = TestArea.forTest(this);
		File from = testArea.newDir("from");
		testArea.hasFile("from/x", "").setExecutable(true);
		testArea.hasFile("from/nonx", "");
		testArea.newDir("from/b");
		testArea.hasFile("from/b/x", "").setExecutable(true);
		testArea.hasFile("from/b/nonx", "");

		File to = testArea.newDir("to");

		FileUtil.copyRecursively(from, to, true);

		assertTrue(new File(to, "x").canExecute());
		assertFalse(new File(to, "nonx").canExecute());
		assertTrue(new File(to, "b/x").canExecute());
		assertFalse(new File(to, "b/nonx").canExecute());
	}

}
