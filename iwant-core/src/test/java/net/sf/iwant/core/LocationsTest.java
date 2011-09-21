package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class LocationsTest extends TestCase {

	public void testLocationsCreatedFromIhave() throws IOException {
		Locations locations = Locations.from(new File("/wsRoot"), new File(
				"/as-x/i-have"), "wsName", new File(
				"/as-x/iwant/cached/blah/iwant-lib"));
		assertEquals(
				"Locations {\n"
						+ "  wsRoot():/wsRoot\n"
						+ "  cacheDir():/as-x/iwant/cached/wsName\n"
						+ "  targetCacheDir():/as-x/iwant/cached/wsName/target\n"
						+ "  contentDescriptionCacheDir():/as-x/iwant/cached/wsName/content-descr\n"
						+ "  temporaryDirectory():/as-x/iwant/cached/wsName/tmp-for-the-only-worker-thread\n"
						+ "  iwantLibs():/as-x/iwant/cached/blah/iwant-lib\n"
						+ "]", locations.toString());
	}

}
