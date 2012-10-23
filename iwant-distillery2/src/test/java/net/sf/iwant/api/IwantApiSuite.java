package net.sf.iwant.api;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantApiSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api");
		suite.addTestSuite(SourceTest.class);
		suite.addTestSuite(ExternalSourceTest.class);
		suite.addTestSuite(HelloTargetTest.class);
		suite.addTestSuite(JavaClassesTest.class);
		suite.addTestSuite(DownloadedTest.class);
		suite.addTestSuite(EclipseSettingsTest.class);
		suite.addTestSuite(ConcatenatedTest.class);
		return suite;
	}

}
