package org.fluentjava.iwant.coreservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.Test;

public class FileUtilTest {

	@Test
	public void relativePathFailsWithDifferentParent() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b is not a child of /a", e.getMessage());
		}
	}

	@Test
	public void relativePathFailsWithFileUnderDifferentParent() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/b/c"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/b/c is not a child of /a", e.getMessage());
		}
	}

	@Test
	public void relativePathFailsWithRoot() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(new File("/"), parent);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("/ is not a child of /a", e.getMessage());
		}
	}

	@Test
	public void relativePathFailsWithNull() {
		File parent = new File("/a");
		try {
			FileUtil.relativePathOfFileUnderParent(null, parent);
			fail();
		} catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void relativePathIsEmptyForParentItself() {
		File parent = new File("/a");
		assertEquals("",
				FileUtil.relativePathOfFileUnderParent(new File("/a"), parent));
	}

	@Test
	public void relativePathIsEmptyForRoot() {
		File parent = new File("/");
		assertEquals("",
				FileUtil.relativePathOfFileUnderParent(new File("/"), parent));
	}

	@Test
	public void relativePathIsBForAB() {
		File parent = new File("/a");
		assertEquals("b", FileUtil
				.relativePathOfFileUnderParent(new File("/a/b"), parent));
	}

	@Test
	public void relativePathIsBCForABC() {
		File parent = new File("/a");
		assertEquals("b/c", FileUtil
				.relativePathOfFileUnderParent(new File("/a/b/c"), parent));
	}

	@Test
	public void copyMissingFilesExcludesSvnMetafiles() throws IOException {
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

	@Test
	public void copyRecursivelyIncludesSvnMetafilesIfToldSo()
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

	@Test
	public void copyRecursivelyPreservedXFlag() throws IOException {
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
