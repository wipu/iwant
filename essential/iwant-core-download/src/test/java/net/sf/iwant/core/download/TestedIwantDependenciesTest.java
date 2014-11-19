package net.sf.iwant.core.download;

import junit.framework.TestCase;

public class TestedIwantDependenciesTest extends TestCase {

	public void testNamesOfDownloadedTargets() {
		// this documents the names, change when upgrading:
		assertEquals("ant-1.9.4.jar", TestedIwantDependencies.antJar().name());
		assertEquals("ant-launcher-1.9.4.jar", TestedIwantDependencies
				.antLauncherJar().name());
		assertEquals("emma-2.0.5312.jar", TestedIwantDependencies.emma().name());
		assertEquals("junit-4.8.2.jar", TestedIwantDependencies.junit().name());
	}

}
