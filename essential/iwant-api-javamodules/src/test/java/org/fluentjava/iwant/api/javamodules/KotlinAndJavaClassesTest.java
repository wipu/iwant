package org.fluentjava.iwant.api.javamodules;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.GnvArtifact;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.entry.Iwant.ExitCalledException;

public class KotlinAndJavaClassesTest extends IwantTestCase {

	private static final KotlinVersion KOTLIN = KotlinVersion._1_3_60();
	private final GnvArtifact<Downloaded> antJar = TestedIwantDependencies
			.antJar();
	private final GnvArtifact<Downloaded> antLauncherJar = TestedIwantDependencies
			.antLauncherJar();

	@Override
	protected boolean mustCaptureSystemOutAndErr() {
		return true;
	}

	@Override
	protected void moreSetUp() throws Exception {
		cacheProvidesRealDownloaded(KOTLIN.kotlinCompilerDistroZip().url());
		KOTLIN.kotlinCompilerDistro().path(ctx);
		KOTLIN.kotlinAntJar().path(ctx);

		cacheProvidesRealDownloaded(antJar.artifact().url());
		antJar.path(ctx);
		cacheProvidesRealDownloaded(antLauncherJar.artifact().url());
		antLauncherJar.path(ctx);
	}

	public void testSrcDirsAreIgredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN, antJar,
				antLauncherJar, Arrays.asList(src1, src2), Arrays.asList(),
				Arrays.asList());

		assertTrue(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	public void testResourceDirsAreIgredients() {
		Path res1 = Source.underWsroot("res1");
		Path res2 = Source.underWsroot("res1");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN, antJar,
				antLauncherJar, Arrays.asList(), Arrays.asList(res1, res2),
				Arrays.asList());

		assertTrue(target.ingredients().contains(res1));
		assertTrue(target.ingredients().contains(res2));
	}

	public void testCrapJavaToPathFails() throws Exception {
		wsRootHasFile("src/Crap.java", "crap");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN, antJar,
				antLauncherJar, Arrays.asList(src), Arrays.asList(),
				Arrays.asList());

		try {
			target.path(ctx);
			fail();
		} catch (ExitCalledException e) {
			assertEquals(null, e.getMessage());
		}
		assertTrue(err().contains("crap"));
	}

	public void testJavaOnlyCompiles() throws Exception {
		wsRootHasFile("src/Minimal.java", "class Minimal {}");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN, antJar,
				antLauncherJar, Arrays.asList(src), Arrays.asList(),
				Arrays.asList());

		target.path(ctx);

		assertTrue(new File(cached, "classes/Minimal.class").exists());
	}

	public void testMixedJavanAndKotlinCompile() throws Exception {
		wsRootHasFile("src/MinimalJava.java", "class MinimalJava {}");
		wsRootHasFile("src/MinimalKotlin.kt", "class MinimalKotlin {}");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN, antJar,
				antLauncherJar, Arrays.asList(src), Arrays.asList(),
				Arrays.asList());

		target.path(ctx);

		assertTrue(new File(cached, "classes/MinimalJava.class").exists());
		assertTrue(new File(cached, "classes/MinimalKotlin.class").exists());
	}

}
