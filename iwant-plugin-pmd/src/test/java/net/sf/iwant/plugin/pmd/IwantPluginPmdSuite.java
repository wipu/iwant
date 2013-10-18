package net.sf.iwant.plugin.pmd;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantPluginPmdSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.plugin.pmd");
		suite.addTestSuite(PmdReportTest.class);
		suite.addTestSuite(CopyPasteReportTest.class);
		return suite;
	}

}
