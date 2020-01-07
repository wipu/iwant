package org.fluentjava.iwant.core.download;

import junit.framework.TestCase;

public class TestedIwantDependenciesTest extends TestCase {

	public void testNamesOfDownloadedTargets() {
		// this documents the names, change when upgrading:
		assertEquals("ant-1.10.7.jar", TestedIwantDependencies.antJar().name());
		assertEquals("ant-launcher-1.10.7.jar",
				TestedIwantDependencies.antLauncherJar().name());
		assertEquals("jcommander-1.48.jar",
				TestedIwantDependencies.jcommander().name());
		assertEquals("junit-4.8.2.jar", TestedIwantDependencies.junit().name());
		assertEquals("testng-6.9.4.jar",
				TestedIwantDependencies.testng().name());
	}

}
