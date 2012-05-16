package net.sf.iwant;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.iwant.api.IwantApiSuite;
import net.sf.iwant.entry3.IwantEntry3Suite;

public class IwantDistillery2Suite {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.distillery2");
		suite.addTest(IwantEntry3Suite.suite());
		suite.addTest(IwantApiSuite.suite());
		return suite;
	}

}
