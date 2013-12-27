package net.sf.iwant.plugin.war;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantPluginWarSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.plugin.war");
		suite.addTestSuite(WarTest.class);
		return suite;
	}

}
