package net.sf.iwant.coreservices;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantCoreservicesSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.coreservices");
		suite.addTestSuite(StreamUtilTest.class);
		suite.addTestSuite(FileUtilTest.class);
		return suite;
	}

}
