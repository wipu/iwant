package net.sf.iwant.eclipsesettings;

import junit.framework.TestCase;

public class OrgEclipseJdtCorePrefsTest extends TestCase {

	/**
	 * No need to test more now that it's 100% constant
	 */
	public void testSomePiecesOfOutput() {
		OrgEclipseJdtCorePrefs p = OrgEclipseJdtCorePrefs.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("eclipse.preferences.version=1\n"));
		assertTrue(actual
				.contains("org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6\n"));
	}

}
