package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.entry.Iwant.ExitCalledException;

import org.apache.commons.io.FileUtils;

public class JacocoCoverageTest extends JacocoTestBase {

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
				.mainClassAndArguments("instrtest.Main", "arg0", "arg1").end();

		assertEquals("[instrtest-classes.jacoco-instr, " + antJar() + ", "
				+ antLauncherJar() + ", " + jacoco() + ", " + asm() + "]",
				coverage.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "jacoco:jacoco-0.7.2.201409121644\n" + "deps:[" + asm()
				+ "]\n" + "antJars:[" + antJar() + ", " + antLauncherJar()
				+ "]\n" + "classLocations:[instrtest-classes.jacoco-instr]\n"
				+ "mainClassName:instrtest.Main\n"
				+ "mainClassArgs:[arg0, arg1]\n" + "mainClassArgsFile:null\n"
				+ "", coverage.contentDescriptor());
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
				.mainClassAndArguments("instrtest.Main", args).end();

		assertEquals("[instrtest-classes.jacoco-instr, " + antJar() + ", "
				+ antLauncherJar() + ", " + jacoco() + ", " + asm()
				+ ", args-file]", coverage.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "jacoco:jacoco-0.7.2.201409121644\n" + "deps:[" + asm()
				+ "]\n" + "antJars:[" + antJar() + ", " + antLauncherJar()
				+ "]\n" + "classLocations:[instrtest-classes.jacoco-instr]\n"
				+ "mainClassName:instrtest.Main\n" + "mainClassArgs:null\n"
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
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("instrtest.Hello").end();
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
				.mainClassAndArguments("instrtest.ArgChecker", "a0", "a1")
				.end();
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
				.mainClassAndArguments("instrtest.ArgChecker", args).end();
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
				.mainClassAndArguments("instrtest.Failer").end();

		try {
			coverage.path(ctx);
			fail();
		} catch (ExitCalledException e) {
			assertNull(e.getMessage());
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

	public void testJvmArgsContainsSaneDefaultsIfNotSpecified()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("instrtest.Hello").end();

		assertEquals("[-XX:-UseSplitVerifier]", coverage.jvmargs().toString());
	}

	public void testJvmArgsCanBeCleared() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("instrtest.Hello").noJvmArgs().end();

		assertEquals("[]", coverage.jvmargs().toString());
	}

	public void testJvmArgsCanBeDefined() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("instrtest.Hello").noJvmArgs()
				.jvmArgs("jvmarg0", "jvmarg1").end();

		assertEquals("[jvmarg0, jvmarg1]", coverage.jvmargs().toString());
	}

	public void testJvmArgsAreUsedInTheAntScriptUnderTmpDir() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar()).with(asm());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacocoWithDeps(jacoco(), asm())
				.mainClassAndArguments("instrtest.Hello")
				.jvmArgs("-Xillegal-jvm-arg").end();
		try {
			coverage.path(ctx);
			fail();
		} catch (ExitCalledException e) {
			// expected, a hackish way of making sure it was used
			assertNull(e.getMessage());
		}

		String scriptContent = contentOf(new File(tmpDir, "coverage.exec.xml"));
		assertTrue(scriptContent
				.contains("<jvmarg value=\"-Xillegal-jvm-arg\"/>"));
	}

}