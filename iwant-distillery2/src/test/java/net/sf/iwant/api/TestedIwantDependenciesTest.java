package net.sf.iwant.api;

import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;

public class TestedIwantDependenciesTest extends TestCase {

	public void testJunitUrlIsTheSameIwantBootstrapperUses() {
		URL expected = Iwant.usingRealNetwork().network().junitUrl();
		Downloaded downloaded = (Downloaded) TestedIwantDependencies.junit();
		assertEquals(expected.toString(), downloaded.url().toString());
	}

	public void testNamesOfDownloadedTargets() {
		// this documents the names, change when upgrading:
		assertEquals("ant-1.7.1.jar", TestedIwantDependencies.antJar().name());
		assertEquals("ant-launcher-1.7.1.jar", TestedIwantDependencies
				.antLauncherJar().name());
		assertEquals("emma-2.0.5312.jar", TestedIwantDependencies.emma().name());
		assertEquals("junit-4.8.2.jar", TestedIwantDependencies.junit().name());
	}

}
