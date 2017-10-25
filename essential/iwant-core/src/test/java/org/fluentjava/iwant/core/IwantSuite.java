package org.fluentjava.iwant.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("org.fluentjava.iwant.core");
		suite.addTestSuite(PathDiggerTest.class);
		suite.addTestSuite(ContentDefinitionsTest.class);
		suite.addTestSuite(LocationsTest.class);
		suite.addTestSuite(RefresherTest.class);
		suite.addTestSuite(PrintPrefixesTest.class);
		suite.addTestSuite(EmbeddedUsageTest.class);
		suite.addTestSuite(IwantTest.class);
		suite.addTestSuite(WorkspaceBuilderTest.class);
		suite.addTestSuite(WorkspaceBuilderArgumentsTest.class);
		return suite;
	}

}
