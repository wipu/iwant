package net.sf.iwant.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.core");
		suite.addTestSuite(PathDiggerTest.class);
		suite.addTestSuite(ContentDefinitionsTest.class);
		suite.addTestSuite(RefresherTest.class);
		suite.addTestSuite(WorkspaceBuilderTest.class);
		return suite;
	}

}
