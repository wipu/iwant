package org.fluentjava.iwant.api.javamodules;

import org.fluentjava.iwant.api.core.SubPath;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.core.download.Downloaded;

public class KotlinVersion {

	private final String version;

	private KotlinVersion(String version) {
		this.version = version;
	}

	public static KotlinVersion _1_3_60() {
		return of("1.3.60");
	}

	public static KotlinVersion of(String value) {
		return new KotlinVersion(value);
	}

	public String compilerDistroUrl() {
		String name = "kotlin-compiler-" + version;
		return "https://github.com/JetBrains/kotlin/releases/download/v"
				+ version + "/" + name + ".zip";
	}

	public Target kotlinAntJar() {
		String name = "kotlin-ant-" + version + ".jar";
		return new SubPath(name, kotlinCompilerDistro(),
				"kotlinc/lib/kotlin-ant.jar");
	}

	public JavaBinModule kotlinStdlib() {
		Target jar = distroFileWithVersionInName(
				"kotlinc/lib/kotlin-stdlib.jar");
		Target src = distroFileWithVersionInName(
				"kotlinc/lib/kotlin-stdlib-sources.jar");
		return JavaBinModule.providing(jar, src).end();
	}

	private Target distroFileWithVersionInName(String relpath) {
		String versionedName = "kotlin-" + version + "-"
				+ relpath.replace("/", "_");
		return new SubPath(versionedName, kotlinCompilerDistro(), relpath);
	}

	public Downloaded kotlinCompilerDistroZip() {
		String name = "kotlin-compiler-" + version;
		return Downloaded.withName(name + ".zip").url(compilerDistroUrl())
				.noCheck();
	}

	public Target kotlinCompilerDistro() {
		Downloaded zip = kotlinCompilerDistroZip();
		return Unzipped.with().name(zip + ".unzipped").from(zip).end();
	}

	public String versionValue() {
		return version;
	}

}
