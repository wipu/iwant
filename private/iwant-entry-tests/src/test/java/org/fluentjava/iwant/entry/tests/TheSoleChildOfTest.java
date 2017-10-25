package org.fluentjava.iwant.entry.tests;

import java.io.File;

import junit.framework.TestCase;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.testarea.TestArea;

public class TheSoleChildOfTest extends TestCase {

	private TestArea testArea;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
	}

	public void testNonDirectoryIsError() {
		File nondir = testArea.hasFile("nondir", "anything");

		try {
			Iwant.theSoleChildOf(nondir);
			fail();
		} catch (IwantException e) {
			assertEquals("The file is not a directory with exactly one child: "
					+ nondir, e.getMessage());
		}
	}

	public void testEmptyDirectoryIsError() {
		File dir = testArea.newDir("empty");

		try {
			Iwant.theSoleChildOf(dir);
			fail();
		} catch (IwantException e) {
			assertEquals("The file is not a directory with exactly one child: "
					+ dir, e.getMessage());
		}
	}

	public void testDirectoryWith2ChildrenIsError() {
		File dir = testArea.newDir("dir");
		testArea.hasFile("dir/1", "1");
		testArea.hasFile("dir/2", "2");

		try {
			Iwant.theSoleChildOf(dir);
			fail();
		} catch (IwantException e) {
			assertEquals("The file is not a directory with exactly one child: "
					+ dir, e.getMessage());
		}
	}

	public void testSoleChildOfDirIsReturned() {
		File dir = testArea.newDir("dir");
		File theone = testArea.hasFile("dir/theone", "theone");

		assertEquals(theone, Iwant.theSoleChildOf(dir));
	}

}
