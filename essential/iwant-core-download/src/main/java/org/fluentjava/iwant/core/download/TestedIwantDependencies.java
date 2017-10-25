package net.sf.iwant.core.download;

import net.sf.iwant.entry2.Iwant2;

public class TestedIwantDependencies {

	public static GnvArtifact<Downloaded> antJar() {
		return FromRepository.repo1MavenOrg().group("org/apache/ant")
				.name("ant").version(Iwant2.ANT_VER).jar();
	}

	public static GnvArtifact<Downloaded> antLauncherJar() {
		return FromRepository.repo1MavenOrg().group("org/apache/ant")
				.name("ant-launcher").version(Iwant2.ANT_VER).jar();
	}

	public static GnvArtifact<Downloaded> jcommander() {
		return FromRepository.repo1MavenOrg().group("com/beust")
				.name("jcommander").version("1.48").jar();
	}

	public static GnvArtifact<Downloaded> junit() {
		return FromRepository.repo1MavenOrg().group("junit").name("junit")
				.version("4.8.2").jar();
	}

	public static GnvArtifact<Downloaded> testng() {
		return FromRepository.repo1MavenOrg().group("org/testng").name("testng")
				.version("6.9.4").jar();
	}

}
