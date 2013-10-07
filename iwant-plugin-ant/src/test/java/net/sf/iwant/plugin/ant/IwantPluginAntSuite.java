package net.sf.iwant.plugin.ant;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantPluginAntSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.plugin.ant");
		suite.addTestSuite(UntarredTest.class);
		suite.addTestSuite(UnzippedTest.class);
		suite.addTestSuite(JarTest.class);
		return suite;
	}

}
