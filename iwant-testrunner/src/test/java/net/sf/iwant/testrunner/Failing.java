package net.sf.iwant.testrunner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Failing extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.testrunner.failing");
		suite.addTestSuite(Tst.class);
		return suite;
	}

	public static class Tst extends TestCase {

		public void testFailure() {
			fail("Simulated test failure to test IwantTestRunner.");
		}

	}

}
