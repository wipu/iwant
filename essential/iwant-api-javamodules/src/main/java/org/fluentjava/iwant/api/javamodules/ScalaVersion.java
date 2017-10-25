package org.fluentjava.iwant.api.javamodules;

import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.download.GnvArtifact;

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
