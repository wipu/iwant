package org.fluentjava.iwant.eclipsesettings;

public class OrgEclipseJdtUiPrefs {

	public static OrgEclipseJdtUiPrefs withDefaultValues() {
		return new OrgEclipseJdtUiPrefs();
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("#Mon May 07 15:52:23 EEST 2012\n");
		b.append("eclipse.preferences.version=1\n");
		b.append("formatter_profile=_iwant-generated\n");
		b.append("formatter_settings_version=12\n");
		return b.toString();
	}

}
