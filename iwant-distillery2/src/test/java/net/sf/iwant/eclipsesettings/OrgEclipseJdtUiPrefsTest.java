package net.sf.iwant.eclipsesettings;

import junit.framework.TestCase;

public class OrgEclipseJdtUiPrefsTest extends TestCase {

	/**
	 * No need to test more now that it's 100% constant
	 */
	public void testSomePiecesOfOutput() {
		OrgEclipseJdtUiPrefs p = OrgEclipseJdtUiPrefs.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("eclipse.preferences.version=1\n"));
		assertTrue(actual.contains("formatter_profile=_iwant-generated\n"));
	}

}
