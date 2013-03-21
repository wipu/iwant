package net.sf.iwant.api;

public class TestedIwantDependencies {

	private static final String ANT_VER = "1.7.1";

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

}
