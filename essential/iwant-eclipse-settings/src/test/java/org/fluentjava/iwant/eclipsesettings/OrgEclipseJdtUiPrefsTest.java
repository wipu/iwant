package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OrgEclipseJdtUiPrefsTest {

	/**
	 * No need to test more now that it's 100% constant
	 */
	@Test
	public void somePiecesOfOutput() {
		OrgEclipseJdtUiPrefs p = OrgEclipseJdtUiPrefs.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("eclipse.preferences.version=1\n"));
		assertTrue(actual.contains("formatter_profile=_iwant-generated\n"));
	}

}
