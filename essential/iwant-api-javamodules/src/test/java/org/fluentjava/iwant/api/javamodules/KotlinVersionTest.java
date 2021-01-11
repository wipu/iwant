package org.fluentjava.iwant.api.javamodules;

import static org.junit.Assert.assertEquals;

import org.fluentjava.iwant.api.core.SubPath;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.core.download.Downloaded;
import org.junit.Test;

public class KotlinVersionTest {

	@Test
	public void getterOfVersionValue() {
		assertEquals("whatever", KotlinVersion.of("whatever").versionValue());
	}

	@Test
	public void compilerDistroUrl() {
		assertEquals(
				"https://github.com/JetBrains/kotlin/releases/download/v1.3.60/"
						+ "kotlin-compiler-1.3.60.zip",
				KotlinVersion._1_3_60().compilerDistroUrl());
		assertEquals(
				"https://github.com/JetBrains/kotlin/releases/download/v1.3.40/"
						+ "kotlin-compiler-1.3.40.zip",
				KotlinVersion.of("1.3.40").compilerDistroUrl());
	}

	@Test
	public void kotlinAntJarIsASubPathUnderUnzippedDistro() {
		KotlinVersion kotlin = KotlinVersion.of("1.3.40");

		SubPath jar = (SubPath) kotlin.kotlinAntJar();

		assertEquals("kotlin-ant-1.3.40.jar", jar.name());
		assertEquals("kotlinc/lib/kotlin-ant.jar", jar.relativePath());

		Unzipped unzippedDistro = (Unzipped) jar.parent();
		Downloaded distro = (Downloaded) unzippedDistro.from();
		assertEquals(kotlin.compilerDistroUrl(), distro.url().toString());
	}

	@Test
	public void stdlibIsABinModuleReferringToJarAndSourcesUnderUnzippedDistro() {
		KotlinVersion kotlin = KotlinVersion._1_3_60();

		JavaBinModule lib = kotlin.kotlinStdlib();

		assertEquals("kotlin-1.3.60-kotlinc_lib_kotlin-stdlib.jar",
				lib.mainArtifact().name());
		assertEquals("kotlin-1.3.60-kotlinc_lib_kotlin-stdlib-sources.jar",
				lib.source().name());
	}

}
