package net.sf.iwant.api.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantApiCoreSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api.core");
		suite.addTestSuite(SubPathTest.class);
		return suite;
	}

}
