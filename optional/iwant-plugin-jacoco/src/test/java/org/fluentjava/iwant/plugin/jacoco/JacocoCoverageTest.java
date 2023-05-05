package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaClassesAndSources;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.SystemEnv;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.entry.Iwant.ExitCalledException;

public class JacocoCoverageTest extends JacocoTestBase {

	public void testParallelismIsDisabledUntilProvenByPracticeItDoesNotCauseProblems() {
		Path classes = Source.underWsroot("classes");
		JacocoInstrumentation instr = JacocoInstrumentation.of(classes)
				.using(jacoco(), antJar(), antLauncherJar());

		JacocoCoverage coverage = JacocoCoverage.with().name("any")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("Any").end();

		assertFalse(coverage.supportsParallelism());
	}

	public void testIngredientsAndDescriptorWithMainClassArgsGivenAsStrings()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Main");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco())
				.mainClassAndArguments("instrtest.Main", "arg0", "arg1").end();

		assertEquals(
				"[" + jacoco() + ", " + antJar() + ", " + antLauncherJar()
						+ ", instrtest-classes.jacoco-instr]",
				coverage.ingredients().toString());
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n  " + antLauncherJar() + "\ni:classLocations:\n"
				+ "  instrtest-classes.jacoco-instr\n" + "p:mainClassName:\n"
				+ "  instrtest.Main\n" + "p:mainClassArgs:\n" + "  arg0\n"
				+ "  arg1\n" + "i:mainClassArgsFile:\n" + " null\n"
				+ "p:jvmargs:\n", coverage.contentDescriptor());
	}

	public void testIngredientsAndDescriptorWithMainClassArgsGivenAsPath()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Main");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());

		Path args = Source.underWsroot("args-file");

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Main", args)
				.end();

		assertEquals(
				"[" + jacoco() + ", " + antJar() + ", " + antLauncherJar()
						+ ", instrtest-classes.jacoco-instr, args-file]",
				coverage.ingredients().toString());
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n  " + antLauncherJar() + "\ni:classLocations:\n"
				+ "  instrtest-classes.jacoco-instr\n" + "p:mainClassName:\n"
				+ "  instrtest.Main\n" + "p:mainClassArgs:\n"
				+ " null-collection\n" + "i:mainClassArgsFile:\n"
				+ "  args-file\n" + "p:jvmargs:\n",
				coverage.contentDescriptor());
	}

	public void testIngredientsAndDescriptorWithJvmArgs() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Main");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).jvmArgs("-Xmx1G")
				.mainClassAndArguments("instrtest.Main", "arg0", "arg1").end();

		assertEquals(
				"[" + jacoco() + ", " + antJar() + ", " + antLauncherJar()
						+ ", instrtest-classes.jacoco-instr]",
				coverage.ingredients().toString());
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n  " + antLauncherJar() + "\ni:classLocations:\n"
				+ "  instrtest-classes.jacoco-instr\n" + "p:mainClassName:\n"
				+ "  instrtest.Main\n" + "p:mainClassArgs:\n" + "  arg0\n"
				+ "  arg1\n" + "i:mainClassArgsFile:\n" + " null\n"
				+ "p:jvmargs:\n" + "  -Xmx1G\n", coverage.contentDescriptor());
	}

	public void testItProducesTheRequestedExecFile() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Hello")
				.end();
		coverage.path(ctx);

		File cachedExec = new File(cached, "coverage.exec");
		assertTrue(cachedExec.exists());
		assertTrue(FileUtils.readFileToByteArray(cachedExec).length > 0);
	}

	/**
	 * We cannot we assert err() because jacoco forks a new jvm process
	 */
	public void testItCallsGivenClassWithArgsGivenAsStrings() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgChecker", "if(!\"[a0, a1]\".equals("
						+ "java.util.Arrays.toString(args))) System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco())
				.mainClassAndArguments("instrtest.ArgChecker", "a0", "a1")
				.end();
		coverage.path(ctx);

		assertTrue(new File(cached, "coverage.exec").exists());
	}

	/**
	 * We cannot we assert err() because jacoco forks a new jvm process
	 */
	public void testItCallsGivenClassWithArgsGivenAsPath() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgChecker", "if(!\"[fa0, fa1]\".equals("
						+ "java.util.Arrays.toString(args))) System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		Target args = new HelloTarget("args", "fa0\nfa1\n");
		args.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco())
				.mainClassAndArguments("instrtest.ArgChecker", args).end();
		coverage.path(ctx);

		assertTrue(new File(cached, "coverage.exec").exists());
	}

	public void testItFailsIfMainClassMakeNonZeroExit() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Failer", "System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Failer")
				.end();

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
				.using(jacoco(), antJar(), antLauncherJar());
		instr1.path(ctx);

		Path classes2 = Source.underWsroot("classes2");

		JavaClassesAndSources cs2 = newJavaClassesAndSources("two", "Two");
		JacocoInstrumentation instr2 = JacocoInstrumentation.of(cs2.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr2.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("cov")
				.antJars(antJar(), antLauncherJar()).jacoco(jacoco())
				.mainClassAndArguments("JunitReferrer").classLocations(classes1)
				.classLocations(instr1).classLocations(classes2)
				.classLocations(instr2).end();

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
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Hello")
				.end();

		assertEquals("[]", coverage.jvmargs().toString());
	}

	public void testJvmArgsCanBeCleared() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Hello")
				.noJvmArgs().end();

		assertEquals("[]", coverage.jvmargs().toString());
	}

	public void testJvmArgsCanBeDefined() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Hello")
				.noJvmArgs().jvmArgs("jvmarg0", "jvmarg1").end();

		assertEquals("[jvmarg0, jvmarg1]", coverage.jvmargs().toString());
	}

	public void testJvmArgsAreUsedInTheAntScriptUnderTmpDir() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.Hello")
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

	/**
	 * We cannot we assert err() because jacoco forks a new jvm process
	 */
	public void testItCallsGivenClassWithGivenEnvVariables() throws Exception {
		Path src = Source.underWsroot("aSrc");

		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "EnvChecker",
				"if(!\"[aString, " + wsRoot + "/aSrc]\".equals("
						+ "java.util.Arrays.asList("
						+ "System.getenv(\"string\"),System.getenv(\"source\")"
						+ ").toString())) System.exit(1);");
		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesAndSources.classes())
				.using(jacoco(), antJar(), antLauncherJar());
		instr.path(ctx);

		SystemEnv env = SystemEnv.with().string("string", "aString")
				.path("source", src).end();
		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.EnvChecker")
				.env(env).end();
		coverage.path(ctx);

		assertTrue(new File(cached, "coverage.exec").exists());
	}

	public void testEnvIsUsedAsIngredientsAndParameters() {
		Path pathForEnv = Source.underWsroot("aSrc");
		Path classesToInstrument = Source.underWsroot("classes");

		JacocoInstrumentation instr = JacocoInstrumentation
				.of(classesToInstrument)
				.using(jacoco(), antJar(), antLauncherJar());

		SystemEnv env = SystemEnv.with().string("string", "aString")
				.path("source", pathForEnv).end();
		JacocoCoverage coverage = JacocoCoverage.with().name("coverage.exec")
				.classLocations(instr).antJars(antJar(), antLauncherJar())
				.jacoco(jacoco()).mainClassAndArguments("instrtest.EnvChecker")
				.env(env).end();

		assertTrue(coverage.contentDescriptor().contains("p:env:string:\n"
				+ "  aString\n" + "i:env:source:\n" + "  aSrc"));
	}

}
