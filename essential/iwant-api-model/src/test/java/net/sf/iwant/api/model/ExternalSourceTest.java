package net.sf.iwant.api.model;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ExternalSourceTest extends TestCase {

	public void testAbsolutePathToString() throws IOException {
		assertEquals("/an/absolute/path", new ExternalSource(new File(
				"/an/absolute/path")).toString());
	}

	/**
	 * Relative paths shouldn't be used but this is how they work
	 */
	public void testRelativePathToString() throws IOException {
		String cwd = System.getProperty("user.dir");
		assertEquals(cwd + "/relative/path", new ExternalSource(new File(
				"relative/path")).toString());
	}

	public void testItHasNoIngredients() throws IOException {
		assertTrue(new ExternalSource(new File("/whatever")).ingredients()
				.isEmpty());
	}

}
