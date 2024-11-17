package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TheSoleChildOfTest {

	private TestArea testArea;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
	}

	@Test
	public void nonDirectoryIsError() {
		File nondir = testArea.hasFile("nondir", "anything");

		try {
			Iwant.theSoleChildOf(nondir);
			fail();
		} catch (IwantException e) {
			assertEquals("The file is not a directory with exactly one child: "
					+ nondir, e.getMessage());
		}
	}

	@Test
	public void emptyDirectoryIsError() {
		File dir = testArea.newDir("empty");

		try {
			Iwant.theSoleChildOf(dir);
			fail();
		} catch (IwantException e) {
			assertEquals("The file is not a directory with exactly one child: "
					+ dir, e.getMessage());
		}
	}

	@Test
	public void directoryWith2ChildrenIsError() {
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

	@Test
	public void soleChildOfDirIsReturned() {
		File dir = testArea.newDir("dir");
		File theone = testArea.hasFile("dir/theone", "theone");

		assertEquals(theone, Iwant.theSoleChildOf(dir));
	}

}
