package net.sf.iwant.api;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantApiSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api");
		suite.addTestSuite(HelloTargetTest.class);
		return suite;
	}

}
