package net.sf.iwant.testrunner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Succeeding extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.testrunner.succeeding");
		suite.addTestSuite(Tst.class);
		return suite;
	}

	public static class Tst extends TestCase {

		private static boolean wasRun;

		public void testSuccess() {
			wasRun = true;
		}

		public static boolean wasRun() {
			return wasRun;
		}

	}

}
