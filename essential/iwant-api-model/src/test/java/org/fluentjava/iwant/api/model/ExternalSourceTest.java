package org.fluentjava.iwant.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ExternalSourceTest {

	@Test
	public void absolutePathToString() {
		assertEquals("/an/absolute/path",
				ExternalSource.at("/an/absolute/path").toString());
	}

	@Test
	public void locationReturnsFileGivenToFileFactory() {
		File location = new File("/location");
		assertSame(location, ExternalSource.at(location).location());
	}

	@Test
	public void locationReturnsGivenPathWhenUsingStringFactory() {
		String location = "/location";
		assertEquals(location,
				ExternalSource.at(location).location().getAbsolutePath());
	}

	/**
	 * Relative paths shouldn't be used but this is how they work
	 */
	@Test
	public void relativePathToString() {
		String cwd = System.getProperty("user.dir");
		assertEquals(cwd + "/relative/path",
				ExternalSource.at("relative/path").toString());
	}

	@Test
	public void itHasNoIngredients() {
		assertTrue(ExternalSource.at("/whatever").ingredients().isEmpty());
	}

	@Test
	public void canonicalPathFailureIsWrappedAsIllegalStateException() {
		try {
			@SuppressWarnings("unused")
			ExternalSource s = ExternalSource.at("\u0000");
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Cannot get canonical path of \u0000", e.getMessage());
			assertTrue(e.getCause() instanceof IOException);
		}
	}

}
