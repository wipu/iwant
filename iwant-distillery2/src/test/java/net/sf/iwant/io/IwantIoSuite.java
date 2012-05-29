package net.sf.iwant.io;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantIoSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.io");
		suite.addTestSuite(StreamUtilTest.class);
		return suite;
	}

}
