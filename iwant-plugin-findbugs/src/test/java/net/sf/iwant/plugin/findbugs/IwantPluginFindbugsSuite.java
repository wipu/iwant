package net.sf.iwant.plugin.findbugs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantPluginFindbugsSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.plugin.findbugs");
		suite.addTestSuite(FindbugsReportTest.class);
		return suite;
	}

}
