package org.fluentjava.iwant.eclipsesettings;

public class OrgEclipseCoreResourcesPrefs {

	public static OrgEclipseCoreResourcesPrefs withDefaultValues() {
		return new OrgEclipseCoreResourcesPrefs();
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("eclipse.preferences.version=1\n");
		b.append("encoding/<project>=UTF-8\n");
		return b.toString();
	}

}
