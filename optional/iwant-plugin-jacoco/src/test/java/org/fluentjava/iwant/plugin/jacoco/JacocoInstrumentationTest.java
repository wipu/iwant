package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;

public class JacocoInstrumentationTest extends JacocoTestBase {

	public void testNameIsDerivedFromTheNameOfJavaClassesAndSourcesPair() {
		assertEquals("one.jacoco-instr",
				JacocoInstrumentation.of(Source.underWsroot("one"))
						.using(jacoco(), antJar(), antLauncherJar()).with(asm())
						.name());
		assertEquals("two.jacoco-instr",
				JacocoInstrumentation.of(Source.underWsroot("two"))
						.using(jacoco(), antJar(), antLauncherJar()).with(asm())
						.name());
	}

	public void testIngredients() {
		assertEquals(
				"[" + jacoco() + ", " + asm() + ", " + antJar() + ", "
						+ antLauncherJar() + ", classes]",
				JacocoInstrumentation.of(Source.underWsroot("classes"))
						.using(jacoco(), antJar(), antLauncherJar()).with(asm())
						.ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals(
				"org.fluentjava.iwant.plugin.jacoco.JacocoInstrumentation\n"
						+ "i:jacoco:\n" + "  jacoco-0.7.2.201409121644\n"
						+ "i:deps:\n" + "  " + asm() + "\ni:antJars:\n" + "  "
						+ antJar() + "\n  " + antLauncherJar()
						+ "\ni:classes:\n" + "  classes\n" + "",
				JacocoInstrumentation.of(Source.underWsroot("classes"))
						.using(jacoco(), antJar(), antLauncherJar()).with(asm())
						.contentDescriptor());
	}

	public void testInstrumentationCreatesAModifiedClass() throws Exception {
		wsRootHasFile("src/pak/Foo.java",
				"package pak;\npublic class Foo {}\n");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(Source.underWsroot("src")).end();
		classes.path(ctx);
		File originalClass = new File(ctx.cached(classes), "pak/Foo.class");
		assertTrue(originalClass.exists());

		JacocoInstrumentation instr = JacocoInstrumentation.of(classes)
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		File instrClass = new File(ctx.cached(instr), "pak/Foo.class");
		assertTrue(instrClass.exists());

		assertFalse(FileUtils.contentEquals(originalClass, instrClass));
	}

	public void testInstrumentationCreatesAModifiedClassAndCopiesResource()
			throws Exception {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.mainResources("res").end();
		wsRootHasFile("mod/src/pak/Foo.java",
				"package pak;\npublic class Foo {}\n");
		wsRootHasFile("mod/res/pak/res.txt", "resource content\n");
		Target classes = (Target) mod.mainArtifact();
		classes.path(ctx);
		File originalClass = new File(ctx.cached(classes), "pak/Foo.class");
		File originalResource = new File(ctx.cached(classes), "pak/res.txt");
		assertTrue(originalClass.exists());
		assertTrue(originalResource.exists());

		JacocoInstrumentation instr = JacocoInstrumentation.of(classes)
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		File instrClass = new File(ctx.cached(instr), "pak/Foo.class");
		assertTrue(instrClass.exists());
		assertFalse(FileUtils.contentEquals(originalClass, instrClass));

		File instrResource = new File(ctx.cached(instr), "pak/res.txt");
		assertTrue(instrResource.exists());
		assertTrue(FileUtils.contentEquals(originalResource, instrResource));
	}

}
