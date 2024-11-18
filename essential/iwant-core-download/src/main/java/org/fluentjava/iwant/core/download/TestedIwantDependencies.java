package org.fluentjava.iwant.core.download;

import java.util.List;

import org.fluentjava.iwant.entry2.Iwant2;

public class TestedIwantDependencies {

	public static final String ANT_VER = Iwant2.ANT_VER;
	public static final String JUNIT_PLATFORM_VER = "1.10.2";
	public static final String JUNIT_JUPITER_VER = "5.10.2";

	private static GnvArtifact<Downloaded> gnv(String g, String n, String v) {
		return FromRepository.repo1MavenOrg().group(g).name(n).version(v).jar();
	}

	public static List<GnvArtifact<Downloaded>> junitJupiterCompileDeps() {
		return List.of(hamcrestCore(), junit(), junitJupiter(),
				junitJupiterApi());
	}

	public static List<GnvArtifact<Downloaded>> junitJupiterRtDeps() {
		return List.of(junitJupiterEngine(), junitPlatformCommons(),
				junitJupiterParams(), junitPlatformConsole(),
				junitPlatformLauncher(), junitPlatformEngine(),
				junitVintageEngine(), opentest4j());
	}

	public static GnvArtifact<Downloaded> antJar() {
		return gnv("org.apache.ant", "ant", ANT_VER);
	}

	public static GnvArtifact<Downloaded> antLauncherJar() {
		return gnv("org.apache.ant", "ant-launcher", ANT_VER);
	}

	public static GnvArtifact<Downloaded> jcommander() {
		return gnv("com.beust", "jcommander", "1.82");
	}

	public static GnvArtifact<Downloaded> commonsIo() {
		return gnv("commons-io", "commons-io", "2.17.0");
	}

	public static GnvArtifact<Downloaded> junit() {
		return gnv("junit", "junit", "4.13.2");
	}

	public static GnvArtifact<Downloaded> junitPlatformLauncher() {
		return gnv("org.junit.platform", "junit-platform-launcher",
				JUNIT_PLATFORM_VER);
	}

	public static GnvArtifact<Downloaded> junitPlatformConsole() {
		return gnv("org.junit.platform", "junit-platform-console",
				JUNIT_PLATFORM_VER);
	}

	public static GnvArtifact<Downloaded> testng() {
		return gnv("org.testng", "testng", "6.9.4");
	}

	public static GnvArtifact<Downloaded> hamcrestCore() {
		return gnv("org.hamcrest", "hamcrest-core", "1.3");
	}

	public static GnvArtifact<Downloaded> junitPlatformCommons() {
		return gnv("org.junit.platform", "junit-platform-commons",
				JUNIT_PLATFORM_VER);
	}

	public static GnvArtifact<Downloaded> junitPlatformEngine() {
		return gnv("org.junit.platform", "junit-platform-engine",
				JUNIT_PLATFORM_VER);
	}

	public static GnvArtifact<Downloaded> junitJupiter() {
		return gnv("org.junit.jupiter", "junit-jupiter", JUNIT_JUPITER_VER);
	}

	public static GnvArtifact<Downloaded> junitJupiterApi() {
		return gnv("org.junit.jupiter", "junit-jupiter-api", JUNIT_JUPITER_VER);
	}

	public static GnvArtifact<Downloaded> junitJupiterEngine() {
		return gnv("org.junit.jupiter", "junit-jupiter-engine",
				JUNIT_JUPITER_VER);
	}

	public static GnvArtifact<Downloaded> junitJupiterParams() {
		return gnv("org.junit.jupiter", "junit-jupiter-params",
				JUNIT_JUPITER_VER);
	}

	/**
	 * NOTE: Without this junit5runner won't run old tests written for junit 3
	 * or 4.
	 */
	public static GnvArtifact<Downloaded> junitVintageEngine() {
		return gnv("org.junit.vintage", "junit-vintage-engine",
				JUNIT_JUPITER_VER);
	}

	public static GnvArtifact<Downloaded> opentest4j() {
		return gnv("org.opentest4j", "opentest4j", "1.3.0");
	}

	public static GnvArtifact<Downloaded> asm() {
		return gnv("asm", "asm", "3.2");
	}

	public static GnvArtifact<Downloaded> jaxen() {
		return gnv("jaxen", "jaxen", "1.1.4");
	}

	public static GnvArtifact<Downloaded> pmd() {
		return gnv("pmd", "pmd", "4.3");
	}

}
