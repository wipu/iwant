package org.fluentjava.iwant.eclipsesettings;

import junit.framework.TestCase;

public class OrgJetbrainsKotlinCorePrefsTest extends TestCase {

	/**
	 * No need to test more now that it's 100% constant
	 */
	public void testSomePiecesOfOutput() {
		OrgJetbrainsKotlinCorePrefs p = OrgJetbrainsKotlinCorePrefs
				.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("codeStyle/codeStyleId=KOTLIN_OFFICIAL\n"));
	}

}
