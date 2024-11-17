package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Properties;

import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.junit.jupiter.api.Test;

public class UserPrefsImplTest {

	@SuppressWarnings("unused")
	@Test
	public void missingWorkerCount() {
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
	@Test
	public void nonIntegerWorkerCount() {
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
	@Test
	public void zeroWorkerCount() {
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

	@Test
	public void validWorkerCount() {
		File file = new File("any");
		Properties p = new Properties();
		p.put("workerCount", "4");
		UserPrefsImpl userPrefs = new UserPrefsImpl(p, file);

		assertEquals(4, userPrefs.workerCount());
		assertEquals("user preferences from file any:\n[workerCount=4]",
				userPrefs.toString());
	}

}
