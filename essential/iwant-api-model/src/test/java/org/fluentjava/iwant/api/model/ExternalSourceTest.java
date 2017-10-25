package org.fluentjava.iwant.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExternalSourceTest {

	@Test
	public void absolutePathToString() {
		assertEquals("/an/absolute/path",
				new ExternalSource(new File("/an/absolute/path")).toString());
	}

	/**
	 * Relative paths shouldn't be used but this is how they work
	 */
	@Test
	public void relativePathToString() {
		String cwd = System.getProperty("user.dir");
		assertEquals(cwd + "/relative/path",
				new ExternalSource(new File("relative/path")).toString());
	}

	@Test
	public void itHasNoIngredients() {
		assertTrue(new ExternalSource(new File("/whatever")).ingredients()
				.isEmpty());
	}

	@Test
	public void canonicalPathFailureIsWrappedAsIllegalStateException() {
		try {
			@SuppressWarnings("unused")
			ExternalSource s = new ExternalSource(new File("\u0000"));
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Cannot get canonical path of \u0000", e.getMessage());
			assertTrue(e.getCause() instanceof IOException);
		}
	}

}
