package net.sf.iwant.core.download;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantCoreDownloadSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.core.download");
		suite.addTestSuite(DownloadedTest.class);
		suite.addTestSuite(FromRepositoryTest.class);
		suite.addTestSuite(TestedIwantDependenciesTest.class);
		return suite;
	}

}
