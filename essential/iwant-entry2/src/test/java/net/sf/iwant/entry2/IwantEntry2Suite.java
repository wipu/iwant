package net.sf.iwant.entry2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantEntry2Suite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.entry2");
		suite.addTestSuite(FileFilterTest.class);
		suite.addTestSuite(TimestampHandlerTest.class);
		suite.addTestSuite(Iwant2Test.class);
		return suite;
	}

}
