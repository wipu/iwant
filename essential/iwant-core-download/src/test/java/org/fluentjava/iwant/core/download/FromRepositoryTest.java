package org.fluentjava.iwant.core.download;

import junit.framework.TestCase;

public class FromRepositoryTest extends TestCase {

	public void testFactsOfArtifactGivenWithSlashesInGroup() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("org/apache/ant").name("ant").version("1.7.1").jar();

		assertEquals("ant-1.7.1.jar", t.name());
		assertEquals("http://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("org.apache.ant", t.group());
		assertEquals("ant", t.shortName());
		assertEquals("1.7.1", t.version());

		assertEquals(
				"http://repo1.maven.org/maven2/"
						+ "org/apache/ant/ant/1.7.1/ant-1.7.1.jar",
				t.artifact().url().toString());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	public void testFactsOfArtifactGivenWithDotsInGroup() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("com.google.code.findbugs").name("findbugs")
				.version("1.3.9").jar();

		assertEquals("findbugs-1.3.9.jar", t.name());
		assertEquals("http://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("com.google.code.findbugs", t.group());
		assertEquals("findbugs", t.shortName());
		assertEquals("1.3.9", t.version());

		assertEquals("http://repo1.maven.org/maven2/"
				+ "com/google/code/findbugs/findbugs/1.3.9/findbugs-1.3.9.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	public void testFactsOfSourceArtifact() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("com.google.code.findbugs").name("findbugs")
				.version("1.3.9").sourcesJar();

		assertEquals("findbugs-1.3.9-sources.jar", t.name());
		assertEquals("http://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("com.google.code.findbugs", t.group());
		assertEquals("findbugs", t.shortName());
		assertEquals("1.3.9", t.version());

		assertEquals("http://repo1.maven.org/maven2/"
				+ "com/google/code/findbugs/findbugs/1.3.9/findbugs-1.3.9-sources.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

}
