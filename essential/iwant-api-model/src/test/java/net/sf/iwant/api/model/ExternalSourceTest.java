package net.sf.iwant.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExternalSourceTest {

	@Test
	public void absolutePathToString() throws IOException {
		assertEquals("/an/absolute/path", new ExternalSource(new File(
				"/an/absolute/path")).toString());
	}

	/**
	 * Relative paths shouldn't be used but this is how they work
	 */
	@Test
	public void relativePathToString() throws IOException {
		String cwd = System.getProperty("user.dir");
		assertEquals(cwd + "/relative/path", new ExternalSource(new File(
				"relative/path")).toString());
	}

	@Test
	public void itHasNoIngredients() throws IOException {
		assertTrue(new ExternalSource(new File("/whatever")).ingredients()
				.isEmpty());
	}

}
