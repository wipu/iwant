package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class OrgJetbrainsKotlinCorePrefsTest {

	/**
	 * No need to test more now that it's 100% constant
	 */
	@Test
	public void somePiecesOfOutput() {
		OrgJetbrainsKotlinCorePrefs p = OrgJetbrainsKotlinCorePrefs
				.withDefaultValues();
		String actual = p.asFileContent();
		assertTrue(actual.contains("codeStyle/codeStyleId=KOTLIN_OFFICIAL\n"));
	}

}
