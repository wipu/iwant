package org.fluentjava.iwant.core.download;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class FromRepositoryTest {

	@Test
	public void factsOfArtifactGivenWithSlashesInGroup() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("org/apache/ant").name("ant").version("1.7.1").jar();

		assertEquals("ant-1.7.1.jar", t.name());
		assertEquals("https://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("org.apache.ant", t.group());
		assertEquals("ant", t.shortName());
		assertEquals("1.7.1", t.version());

		assertEquals(
				"https://repo1.maven.org/maven2/"
						+ "org/apache/ant/ant/1.7.1/ant-1.7.1.jar",
				t.artifact().url().toString());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	@Test
	public void factsOfArtifactGivenWithDotsInGroup() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("com.google.code.findbugs").name("findbugs")
				.version("1.3.9").jar();

		assertEquals("findbugs-1.3.9.jar", t.name());
		assertEquals("https://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("com.google.code.findbugs", t.group());
		assertEquals("findbugs", t.shortName());
		assertEquals("1.3.9", t.version());

		assertEquals("https://repo1.maven.org/maven2/"
				+ "com/google/code/findbugs/findbugs/1.3.9/findbugs-1.3.9.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	@Test
	public void factsOfSourceArtifact() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("com.google.code.findbugs").name("findbugs")
				.version("1.3.9").sourcesJar();

		assertEquals("findbugs-1.3.9-sources.jar", t.name());
		assertEquals("https://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("com.google.code.findbugs", t.group());
		assertEquals("findbugs", t.shortName());
		assertEquals("1.3.9", t.version());

		assertEquals("https://repo1.maven.org/maven2/"
				+ "com/google/code/findbugs/findbugs/1.3.9/findbugs-1.3.9-sources.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	@Test
	public void factsOfTestArtifact() {
		GnvArtifact<Downloaded> t = FromRepository.repo1MavenOrg()
				.group("org.apache.kafka").name("kafka_2.11").version("2.0.1")
				.testJar();

		assertEquals("kafka_2.11-2.0.1-test.jar", t.name());
		assertEquals("https://repo1.maven.org/maven2/", t.urlPrefix());
		assertEquals("org.apache.kafka", t.group());
		assertEquals("kafka_2.11", t.shortName());
		assertEquals("2.0.1", t.version());

		assertEquals("https://repo1.maven.org/maven2/"
				+ "org/apache/kafka/kafka_2.11/2.0.1/kafka_2.11-2.0.1-test.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

	@Test
	public void factsWithCustomUrlPrefix() {
		GnvArtifact<Downloaded> t = FromRepository
				.at("https://maven.google.com/").group("androidx.annotation")
				.name("annotation").version("1.1.0").jar();

		assertEquals("annotation-1.1.0.jar", t.name());
		assertEquals("https://maven.google.com/", t.urlPrefix());
		assertEquals("androidx.annotation", t.group());
		assertEquals("annotation", t.shortName());
		assertEquals("1.1.0", t.version());

		assertEquals("https://maven.google.com/"
				+ "androidx/annotation/annotation/1.1.0/annotation-1.1.0.jar",
				t.artifact().url().toExternalForm());
		// TODO refer to correct checksum url
		assertNull(t.artifact().md5());
	}

}
