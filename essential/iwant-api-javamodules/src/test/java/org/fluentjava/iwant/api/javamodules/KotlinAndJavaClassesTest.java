package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.junit.jupiter.api.Test;

public class KotlinAndJavaClassesTest extends IwantTestCase {

	private static final KotlinVersion KOTLIN = KotlinVersion._1_3_60();

	@Override
	protected boolean mustCaptureSystemOutAndErr() {
		return true;
	}

	@Override
	protected void moreSetUp() throws Exception {
		cacheProvidesRealDownloaded(KOTLIN.kotlinCompilerDistroZip().url());
		KOTLIN.kotlinCompilerDistro().path(ctx);
		KOTLIN.kotlinAntJar().path(ctx);
	}

	@Test
	public void srcDirsAreIgredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src1");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN,
				Arrays.asList(src1, src2), Arrays.asList(), Arrays.asList());

		assertTrue(target.ingredients().contains(src1));
		assertTrue(target.ingredients().contains(src2));
	}

	@Test
	public void resourceDirsAreIgredients() {
		Path res1 = Source.underWsroot("res1");
		Path res2 = Source.underWsroot("res1");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN,
				Arrays.asList(), Arrays.asList(res1, res2), Arrays.asList());

		assertTrue(target.ingredients().contains(res1));
		assertTrue(target.ingredients().contains(res2));
	}

	@Test
	public void crapJavaToPathFails() throws Exception {
		wsRootHasFile("src/Crap.java", "crap");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN,
				Arrays.asList(src), Arrays.asList(), Arrays.asList());

		try {
			target.path(ctx);
			fail();
		} catch (IwantException e) {
			assertEquals("Script exited with non-zero status 1",
					e.getMessage());
		}
		assertTrue(err().contains("crap"));
	}

	@Test
	public void javaOnlyCompiles() throws Exception {
		wsRootHasFile("src/Minimal.java", "class Minimal {}");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN,
				Arrays.asList(src), Arrays.asList(), Arrays.asList());

		target.path(ctx);

		assertTrue(new File(cached, "classes/Minimal.class").exists());
	}

	@Test
	public void mixedJavanAndKotlinCompile() throws Exception {
		wsRootHasFile("src/MinimalJava.java", "class MinimalJava {}");
		wsRootHasFile("src/MinimalKotlin.kt", "class MinimalKotlin {}");
		Source src = Source.underWsroot("src");
		Target target = new KotlinAndJavaClasses("classes", KOTLIN,
				Arrays.asList(src), Arrays.asList(), Arrays.asList());

		target.path(ctx);

		assertTrue(new File(cached, "classes/MinimalJava.class").exists());
		assertTrue(new File(cached, "classes/MinimalKotlin.class").exists());
	}

	/**
	 * Using AntRunner leaked memory. It is either kotlinc itself or its ant
	 * adapter. Now we fork a new process via a shell script.
	 */
	@Test
	public void targetDoesRunOutOfMemoryWhenRepeated() throws Exception {
		if (true) {
			System.err
					.println("Not running the slow testTargetDoesNotLeakMemory"
							+ " unless temporarily enabled.");
			return;
		}
		@SuppressWarnings("unused")
		Source src = Source.underWsroot("src");
		int iterationCount = 100;
		for (int i = 0; i < iterationCount; i++) {
			wsRootHasFile("src/MinimalJava.java", "class MinimalJava {}");
			wsRootHasFile("src/MinimalKotlin.kt", "class MinimalKotlin {}");
			Target target = new KotlinAndJavaClasses("classes", KOTLIN,
					Arrays.asList(src), Arrays.asList(), Arrays.asList());
			target.path(ctx);

			assertTrue(new File(cached, "classes/MinimalJava.class").exists());
			assertTrue(
					new File(cached, "classes/MinimalKotlin.class").exists());

			Runtime rt = Runtime.getRuntime();
			rt.gc();
			long fm = rt.freeMemory();
			System.out.println(fm);
		}

	}

}
