package net.sf.iwant.api;

import java.io.File;
import java.net.URL;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.entry.Iwant;

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

	public static Path junit() {
		URL url = Iwant.usingRealNetwork().network().junitUrl();
		File file = new File(url.getFile());
		return Downloaded.withName(file.getName()).url(url).noCheck();
	}

}
