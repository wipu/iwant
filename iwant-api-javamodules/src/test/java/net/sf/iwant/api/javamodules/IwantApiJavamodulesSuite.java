package net.sf.iwant.api.javamodules;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantApiJavamodulesSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.api.javamodules");
		suite.addTestSuite(JavaSrcModuleTest.class);
		return suite;
	}

}
