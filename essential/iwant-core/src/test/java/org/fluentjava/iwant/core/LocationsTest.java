package org.fluentjava.iwant.core;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class LocationsTest{

	@Test public void locationsCreatedFromIhave() throws IOException {
		Locations locations = Locations.from(new File("/wsRoot"), new File(
				"/as-x/i-have"), "wsName", new File(
				"/as-x/with/bash/iwant/cached/blah/iwant-lib"));
		assertEquals(
				"Locations {\n"
						+ "  wsRoot():/wsRoot\n"
						+ "  asSomeone():/as-x\n"
						+ "  iHave():/as-x/i-have\n"
						+ "  iwant():/as-x/with/bash/iwant\n"
						+ "  cacheDir():/as-x/with/bash/iwant/cached/wsName\n"
						+ "  targetCacheDir():/as-x/with/bash/iwant/cached/wsName/target\n"
						+ "  contentDescriptionCacheDir():/as-x/with/bash/iwant/cached/wsName/content-descr\n"
						+ "  temporaryDirectory():/as-x/with/bash/iwant/cached/wsName/tmp-for-the-only-worker-thread\n"
						+ "  iwantLibs():/as-x/with/bash/iwant/cached/blah/iwant-lib\n"
						+ "]", locations.toString());
	}

}
