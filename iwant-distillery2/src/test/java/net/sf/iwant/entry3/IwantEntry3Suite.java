package net.sf.iwant.entry3;

import junit.framework.Test;
import junit.framework.TestSuite;

public class IwantEntry3Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.entry3");
		suite.addTestSuite(Iwant3Test.class);
		return suite;
	}

}
