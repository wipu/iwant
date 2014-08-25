package net.sf.iwant.api;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantApiSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api");
		suite.addTestSuite(HelloTargetTest.class);
		suite.addTestSuite(JavaClassesTest.class);
		suite.addTestSuite(DownloadedTest.class);
		suite.addTestSuite(CodeStylePolicyTest.class);
		suite.addTestSuite(JavaBinModuleTest.class);
		suite.addTestSuite(EclipseSettingsTest.class);
		suite.addTestSuite(ConcatenatedTest.class);
		suite.addTestSuite(FromRepositoryTest.class);
		suite.addTestSuite(AsEmbeddedIwantUserTest.class);
		suite.addTestSuite(AntGeneratedTest.class);
		suite.addTestSuite(ScriptGeneratedTest.class);
		suite.addTestSuite(JavaClassesAndSourcesTest.class);
		suite.addTestSuite(TestedIwantDependenciesTest.class);
		suite.addTestSuite(EmmaInstrumentationTest.class);
		suite.addTestSuite(ClassNameListTest.class);
		suite.addTestSuite(EmmaCoverageTest.class);
		suite.addTestSuite(EmmaTargetsOfJavaModulesTest.class);
		suite.addTestSuite(EmmaReportTest.class);
		suite.addTestSuite(CodeFormatterPolicyTest.class);
		suite.addTestSuite(SubPathTest.class);
		return suite;
	}

}
