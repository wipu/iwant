package net.sf.iwant.api;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantApiSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api");
		suite.addTestSuite(BaseIwantWorkspaceTest.class);
		return suite;
	}

}
