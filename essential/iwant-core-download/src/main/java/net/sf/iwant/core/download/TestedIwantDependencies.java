package net.sf.iwant.core.download;

import net.sf.iwant.api.model.Path;

public class TestedIwantDependencies {

	private static final String ANT_VER = "1.9.4";

	public static Path antJar() {
		return FromRepository.ibiblio().group("org/apache/ant").name("ant")
				.version(ANT_VER);
	}

	public static Path antLauncherJar() {
		return FromRepository.ibiblio().group("org/apache/ant")
				.name("ant-launcher").version(ANT_VER);
	}

	public static Path emma() {
		return FromRepository.ibiblio().group("emma").name("emma")
				.version("2.0.5312");
	}

	public static Path jcommander() {
		return FromRepository.ibiblio().group("com/beust").name("jcommander")
				.version("1.48");
	}

	public static Path junit() {
		return FromRepository.ibiblio().group("junit").name("junit")
				.version("4.8.2");
	}

	public static Path testng() {
		return FromRepository.ibiblio().group("org/testng").name("testng")
				.version("6.9.4");
	}

}
