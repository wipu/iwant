package net.sf.iwant.api.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantApiModelSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api.model");
		suite.addTestSuite(ExternalSourceTest.class);
		suite.addTestSuite(SourceTest.class);
		suite.addTestSuite(StringFilterByEqualityTest.class);
		return suite;
	}

}
