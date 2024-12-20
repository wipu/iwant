package org.fluentjava.iwant.core.download;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestedIwantDependenciesTest {

	@Test
	public void namesOfDownloadedTargets() {
		// this documents the names, change when upgrading:
		assertEquals("ant-1.10.14.jar",
				TestedIwantDependencies.antJar().name());
		assertEquals("ant-launcher-1.10.14.jar",
				TestedIwantDependencies.antLauncherJar().name());
		assertEquals("jcommander-1.82.jar",
				TestedIwantDependencies.jcommander().name());
		assertEquals("junit-4.13.2.jar",
				TestedIwantDependencies.junit().name());
		assertEquals("testng-6.9.4.jar",
				TestedIwantDependencies.testng().name());
	}

}
