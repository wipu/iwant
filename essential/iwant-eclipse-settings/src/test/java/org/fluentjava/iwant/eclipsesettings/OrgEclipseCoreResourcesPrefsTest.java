package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OrgEclipseCoreResourcesPrefsTest {

	/**
	 * No need to test more now that it's 100% constant
	 */
	@Test
	public void somePiecesOfOutput() {
		OrgEclipseCoreResourcesPrefs p = OrgEclipseCoreResourcesPrefs
				.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("eclipse.preferences.version=1\n"));
		assertTrue(actual.contains("encoding/<project>=UTF-8\n"));
	}

}