package net.sf.iwant;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.iwant.api.IwantApiSuite;
import net.sf.iwant.eclipsesettings.IwantEclipseSettingsSuite;
import net.sf.iwant.entry3.IwantEntry3Suite;
import net.sf.iwant.planner.IwantPlannerSuite;

public class IwantDistillery2Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.distillery2");
		suite.addTest(IwantApiSuite.suite());
		suite.addTest(IwantEclipseSettingsSuite.suite());
		suite.addTest(IwantPlannerSuite.suite());
		suite.addTest(IwantEntry3Suite.suite());
		return suite;
	}

}
