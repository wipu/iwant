package net.sf.iwant.api.javamodules;

import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.GnvArtifact;

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
		return of("2.11.7");
	}

	public static ScalaVersion of(String value) {
		return new ScalaVersion(value);
	}

	private GnvArtifact<Downloaded> jar(String name) {
		return FromRepository.repo1MavenOrg().group("org.scala-lang").name(name)
				.version(value).jar();
	}

	public GnvArtifact<Downloaded> compilerJar() {
		return jar("scala-compiler");
	}

	public GnvArtifact<Downloaded> libraryJar() {
		return jar("scala-library");
	}

	public GnvArtifact<Downloaded> reflectJar() {
		return jar("scala-reflect");
	}

}
