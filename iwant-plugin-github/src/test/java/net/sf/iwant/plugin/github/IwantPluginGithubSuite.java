package net.sf.iwant.plugin.github;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class IwantPluginGithubSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("net.sf.iwant.plugin.github");
		suite.addTestSuite(FromGithubTest.class);
		return suite;
	}

}
