package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import net.sf.iwant.api.AsEmbeddedIwantUser;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.ExitCalledException;

import org.apache.commons.io.FileUtils;

public class JacocoCoverageTest extends IwantTestCase {

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

	private JavaClassesAndSources newJavaClassesAndSources(String name,
			String className, String... codeLinesForMain) throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);

		StringBuilder code = new StringBuilder();
		code.append("public class " + className + " {\n");
		code.append("  public static void main(String[] args) throws Throwable {\n");
		for (String codeLine : codeLinesForMain) {
			code.append(codeLine).append("\n");
		}
		code.append("  }\n");
		code.append("}\n");

		Iwant.newTextFile(new File(srcDir, className + ".java"),
				code.toString());
		JavaClasses classes = JavaClasses.with().name(name + "-classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		return new JavaClassesAndSources(classes,
				Source.underWsroot(srcDirString));
	}

	// the tests

	public void testParallelismIsDisabledUntilProvenByPracticeItDoesNotCauseProblems()
			throws IOException {
		Path classes = Source.underWsroot("classes");
		JacocoInstrumentation instr = JacocoInstrumentation.of(classes)
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());

		JacocoCoverage coverage = JacocoCoverage.with().name("any")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm()).mainClassAndArguments("Any")
				.end();

		assertFalse(coverage.supportsParallelism());
	}

	public void testIngredientsAndDescriptorWithMainClassArgsGivenAsStrings()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Main");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("Main", "arg0", "arg1").end();

		assertEquals("[instrtest-classes.jacoco-instr, " + antJar() + ", "
				+ antLauncherJar() + ", " + jacoco() + ", " + asm() + "]",
				coverage.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "jacoco:jacoco-0.7.2.201409121644\n" + "deps:[" + asm()
				+ "]\n" + "antJars:[" + antJar() + ", " + antLauncherJar()
				+ "]\n" + "classLocations:[instrtest-classes.jacoco-instr]\n"
				+ "mainClassName:Main\n" + "mainClassArgs:[arg0, arg1]\n"
				+ "mainClassArgsFile:null\n" + "", coverage.contentDescriptor());
	}

	public void testIngredientsAndDescriptorWithMainClassArgsGivenAsPath()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Main");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());

		Path args = Source.underWsroot("args-file");

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("Main", args).end();

		assertEquals("[instrtest-classes.jacoco-instr, " + antJar() + ", "
				+ antLauncherJar() + ", " + jacoco() + ", " + asm()
				+ ", args-file]", coverage.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "jacoco:jacoco-0.7.2.201409121644\n" + "deps:[" + asm()
				+ "]\n" + "antJars:[" + antJar() + ", " + antLauncherJar()
				+ "]\n" + "classLocations:[instrtest-classes.jacoco-instr]\n"
				+ "mainClassName:Main\n" + "mainClassArgs:null\n"
				+ "mainClassArgsFile:args-file\n" + "",
				coverage.contentDescriptor());
	}

	public void testItProducesTheRequestedExecFile() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm()).mainClassAndArguments("Hello")
				.end();
		coverage.path(ctx);

		File cachedExec = new File(cacheDir, "coverage.exec");
		assertTrue(cachedExec.exists());
		assertTrue(FileUtils.readFileToByteArray(cachedExec).length > 0);
	}

	/**
	 * TODO why cannot we assert err() like in EmmaCoverageTest
	 */
	public void testItCallsGivenClassWithArgsGivenAsStrings() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgChecker", "if(!\"[a0, a1]\".equals("
						+ "java.util.Arrays.toString(args))) System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("ArgChecker", "a0", "a1").end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "coverage.exec").exists());
	}

	/**
	 * TODO why cannot we assert err() like in EmmaCoverageTest
	 */
	public void testItCallsGivenClassWithArgsGivenAsPath() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgChecker", "if(!\"[fa0, fa1]\".equals("
						+ "java.util.Arrays.toString(args))) System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		Target args = new HelloTarget("args", "fa0\nfa1\n");
		args.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("ArgChecker", args).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "coverage.exec").exists());
	}

	public void testItFailsIfMainClassMakeNonZeroExit() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Failer", "System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("Failer").end();

		try {
			coverage.path(ctx);
			fail();
		} catch (InvocationTargetException e) {
			assertTrue(e.getCause() instanceof ExitCalledException);
		}

	}

	public void testAllClasspathItemsBothInstrumentedAndNonInstrumentedGoToClasspathInTheSpecifiedOrder()
			throws Exception {
		Path classes1 = Source.underWsroot("classes1");

		JavaClassesAndSources cs1 = newJavaClassesAndSources("one", "One");
		JacocoInstrumentation instr1 = JacocoInstrumentation.of(cs1.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr1.path(ctx);

		Path classes2 = Source.underWsroot("classes2");

		JavaClassesAndSources cs2 = newJavaClassesAndSources("two", "Two");
		JacocoInstrumentation instr2 = JacocoInstrumentation.of(cs2.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr2.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("cov")
				.antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("JunitReferrer")
				.classLocations(classes1).classLocations(instr1)
				.classLocations(classes2).classLocations(instr2).end();

		List<Path> classpath = coverage.classLocations();
		assertEquals(
				"[classes1, one-classes.jacoco-instr, classes2, two-classes.jacoco-instr]",
				classpath.toString());
	}

}
