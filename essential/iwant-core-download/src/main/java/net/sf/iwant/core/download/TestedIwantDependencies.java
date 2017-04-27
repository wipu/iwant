package net.sf.iwant.core.download;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.entry2.Iwant2;

public class TestedIwantDependencies {

	public static Path antJar() {
		return FromRepository.repo1MavenOrg().group("org/apache/ant")
				.name("ant").version(Iwant2.ANT_VER);
	}

	public static Path antLauncherJar() {
		return FromRepository.repo1MavenOrg().group("org/apache/ant")
				.name("ant-launcher").version(Iwant2.ANT_VER);
	}

	public static Path jcommander() {
		return FromRepository.repo1MavenOrg().group("com/beust")
				.name("jcommander").version("1.48");
	}

	public static Path junit() {
		return FromRepository.repo1MavenOrg().group("junit").name("junit")
				.version("4.8.2");
	}

	public static Path testng() {
		return FromRepository.repo1MavenOrg().group("org/testng").name("testng")
				.version("6.9.4");
	}

}
