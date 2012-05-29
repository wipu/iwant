package net.sf.iwant.entry3;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Mock
 */
public class IwantDistillery2Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant");
		suite.addTest(IwantEntry3Suite.suite());
		return suite;
	}

}
