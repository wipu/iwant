package org.fluentjava.iwant.eclipsesettings;

public class OrgJetbrainsKotlinCorePrefs {

	public static OrgJetbrainsKotlinCorePrefs withDefaultValues() {
		return new OrgJetbrainsKotlinCorePrefs();
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("codeStyle/codeStyleId=KOTLIN_OFFICIAL\n");
		b.append("codeStyle/globalsOverridden=true\n");
		b.append("eclipse.preferences.version=1\n");
		b.append("globalsOverridden=true\n");
		return b.toString();
	}

}
