package net.sf.iwant.api.javamodules;

import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.core.download.FromRepository;

public class ScalaVersion {

	private final String value;

	private ScalaVersion(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + value;
	}

	public static ScalaVersion _2_11_7() {
		return new ScalaVersion("2.11.7");
	}

	private Downloaded jar(String name) {
		return FromRepository.repo1MavenOrg().group("org.scala-lang").name(name)
				.version(value);
	}

	public Downloaded compilerJar() {
		return jar("scala-compiler");
	}

	public Downloaded libraryJar() {
		return jar("scala-library");
	}

	public Downloaded reflectJar() {
		return jar("scala-reflect");
	}

}
