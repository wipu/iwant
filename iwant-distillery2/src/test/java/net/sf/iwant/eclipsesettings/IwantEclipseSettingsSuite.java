package net.sf.iwant.eclipsesettings;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantEclipseSettingsSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.eclipsesettings");
		suite.addTestSuite(DotProjectTest.class);
		suite.addTestSuite(DotClasspathTest.class);
		suite.addTestSuite(EclipseAntScriptTest.class);
		suite.addTestSuite(EclipseProjectTest.class);
		suite.addTestSuite(EclipseSettingsWriterTest.class);
		suite.addTestSuite(OrgEclipseJdtCorePrefsTest.class);
		suite.addTestSuite(OrgEclipseJdtUiPrefsTest.class);
		return suite;
	}
}
