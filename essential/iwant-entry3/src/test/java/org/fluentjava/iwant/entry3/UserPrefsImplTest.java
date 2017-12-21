package org.fluentjava.iwant.entry3;

import java.io.File;
import java.util.Properties;

import org.fluentjava.iwant.entry.Iwant.IwantException;

import junit.framework.TestCase;

public class UserPrefsImplTest extends TestCase {

	@SuppressWarnings("unused")
	public void testMissingWorkerCount() {
		File file = new File("any");
		Properties p = new Properties();
		try {
			new UserPrefsImpl(p, file);
			fail();
		} catch (IwantException e) {
			// expected
			assertEquals("Please specify workerCount in " + file,
					e.getMessage());
		}

	}

	@SuppressWarnings("unused")
	public void testNonIntegerWorkerCount() {
		File file = new File("any");
		Properties p = new Properties();
		p.put("workerCount", "1.1");
		try {
			new UserPrefsImpl(p, file);
			fail();
		} catch (IwantException e) {
			// expected
			assertEquals("Please specify workerCount as an integer in " + file,
					e.getMessage());
		}

	}

	@SuppressWarnings("unused")
	public void testZeroWorkerCount() {
		File file = new File("any");
		Properties p = new Properties();
		p.put("workerCount", "0");
		try {
			new UserPrefsImpl(p, file);
			fail();
		} catch (IwantException e) {
			// expected
			assertEquals("Please specify workerCount as a positive integer in "
					+ file, e.getMessage());
		}

	}

	public void testValidWorkerCount() {
		File file = new File("any");
		Properties p = new Properties();
		p.put("workerCount", "4");
		UserPrefsImpl userPrefs = new UserPrefsImpl(p, file);

		assertEquals(4, userPrefs.workerCount());
		assertEquals("user preferences from file any:\n[workerCount=4]",
				userPrefs.toString());
	}

}
