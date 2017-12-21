package org.fluentjava.iwant.api.javamodules;

import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.download.GnvArtifact;

public class ScalaVersion {

	private final String value;
	private final GnvArtifact<Downloaded> compilerJar;
	private final GnvArtifact<Downloaded> libraryJar;
	private final GnvArtifact<Downloaded> reflectJar;

	public ScalaVersion(String value) {
		this.value = value;
		this.compilerJar = jar("scala-compiler", value);
		this.libraryJar = jar("scala-library", value);
		this.reflectJar = jar("scala-reflect", value);
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

	private static GnvArtifact<Downloaded> jar(String name, String version) {
		return FromRepository.repo1MavenOrg().group("org.scala-lang").name(name)
				.version(version).jar();
	}

	public GnvArtifact<Downloaded> compilerJar() {
		return compilerJar;
	}

	public GnvArtifact<Downloaded> libraryJar() {
		return libraryJar;
	}

	public GnvArtifact<Downloaded> reflectJar() {
		return reflectJar;
	}

}
