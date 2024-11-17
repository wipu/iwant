package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.iwant.core.download.Downloaded;
import org.junit.jupiter.api.Test;

public class ScalaVersionTest {

	private static void assertUrl(String url, Downloaded path) {
		assertEquals(url, path.url().toString());
	}

	@Test
	public void jarUrlsOfAVersion() {
		ScalaVersion scala = ScalaVersion._2_11_7();
		assertUrl(
				"https://repo1.maven.org/maven2/org/scala-lang/"
						+ "scala-compiler/2.11.7/scala-compiler-2.11.7.jar",
				scala.compilerJar().artifact());
		assertUrl(
				"https://repo1.maven.org/maven2/org/scala-lang/"
						+ "scala-library/2.11.7/scala-library-2.11.7.jar",
				scala.libraryJar().artifact());
		assertUrl(
				"https://repo1.maven.org/maven2/org/scala-lang/"
						+ "scala-reflect/2.11.7/scala-reflect-2.11.7.jar",
				scala.reflectJar().artifact());
	}

	@Test
	public void toStringOfAVersion() {
		assertEquals("ScalaVersion:2.11.7", ScalaVersion._2_11_7().toString());
	}

}
