package org.fluentjava.iwant.eclipsesettings;

import junit.framework.TestCase;

public class OrgEclipseCoreResourcesPrefsTest extends TestCase {

	/**
	 * No need to test more now that it's 100% constant
	 */
	public void testSomePiecesOfOutput() {
		OrgEclipseCoreResourcesPrefs p = OrgEclipseCoreResourcesPrefs
				.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("eclipse.preferences.version=1\n"));
		assertTrue(actual.contains("encoding/<project>=UTF-8\n"));
	}

}