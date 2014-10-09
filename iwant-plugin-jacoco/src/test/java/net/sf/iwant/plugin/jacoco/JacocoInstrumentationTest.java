package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.AsEmbeddedIwantUser;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;

import org.apache.commons.io.FileUtils;

public class JacocoInstrumentationTest extends IwantTestCase {

	@Override
	protected void moreSetUp() throws Exception {
		caches.cachesUrlAt(jacoco().zip().url(), cachedJacocoZip());
		jacoco().path(ctx);
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(jacoco().zip().url());
	}

	private Path asm() throws IOException {
		return downloaded(FromRepository.repo1MavenOrg().group("org/ow2/asm")
				.name("asm-all").version("5.0.1"));
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	// the tests

	public void testNameIsDerivedFromTheNameOfJavaClassesAndSourcesPair()
			throws IOException {
		assertEquals(
				"one.jacoco-instr",
				JacocoInstrumentation.of(Source.underWsroot("one"))
						.using(jacoco(), antJar(), antLauncherJar())
						.with(asm()).name());
		assertEquals(
				"two.jacoco-instr",
				JacocoInstrumentation.of(Source.underWsroot("two"))
						.using(jacoco(), antJar(), antLauncherJar())
						.with(asm()).name());
	}

	public void testIngredients() throws IOException {
		assertEquals(
				"[classes, " + jacoco() + ", " + antJar() + ", "
						+ antLauncherJar() + ", " + asm() + "]",
				JacocoInstrumentation.of(Source.underWsroot("classes"))
						.using(jacoco(), antJar(), antLauncherJar())
						.with(asm()).ingredients().toString());
	}

	public void testContentDescriptor() throws IOException {
		assertEquals(
				"net.sf.iwant.plugin.jacoco.JacocoInstrumentation\n"
						+ "jacoco:" + jacoco() + "\n" + "deps:[" + asm()
						+ "]\n" + "antJars:[" + antJar() + ", "
						+ antLauncherJar() + "]\n" + "classes:classes\n" + "",
				JacocoInstrumentation.of(Source.underWsroot("classes"))
						.using(jacoco(), antJar(), antLauncherJar())
						.with(asm()).contentDescriptor());
	}

	public void testInstrumentationCreatesAModifiedClass() throws Exception {
		wsRootHasFile("src/pak/Foo.java", "package pak;\npublic class Foo {}\n");
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
