package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EmmaCoverageTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private Iwant iwant;
	private IwantNetwork network;
	private File tmpDir;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock ctx;
	private File cacheDir;

	private PrintStream oldOut;

	private PrintStream oldErr;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		cacheDir = testArea.newDir("cache");
		caches.cachesModifiableTargetsAt(cacheDir);
		tmpDir = new File(testArea.root(), "tmpDir");
		tmpDir.mkdirs();
		caches.providesTemporaryDirectoryAt(tmpDir);
		ctx = new TargetEvaluationContextMock(iwant, caches);

		oldOut = System.out;
		oldErr = System.err;
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	@Override
	protected void tearDown() throws Exception {
		System.setErr(oldErr);
		System.setOut(oldOut);
		System.err.println(err());
	}

	private String err() {
		return err.toString();
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path emma() throws IOException {
		return downloaded(TestedIwantDependencies.emma());
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	private Path junit() throws IOException {
		return downloaded(TestedIwantDependencies.junit());
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
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				Source.underWsroot("classes"), Source.underWsroot("src"));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());

		EmmaCoverage coverage = EmmaCoverage.with().name("any")
				.instrumentations(instr).antJars(antJar(), antLauncherJar())
				.emma(emma()).mainClassAndArguments("Any").end();

		assertFalse(coverage.supportsParallelism());
	}

	public void testIngredients() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Hello").instrumentations(instr)
				.nonInstrumentedClasses(junit()).end();

		assertEquals("[" + antJar() + ", " + antLauncherJar() + ", " + emma()
				+ ", instrtest-classes.emma-instr, " + junit() + "]", coverage
				.ingredients().toString());
	}

	public void testMainClassArgumentPathIsAnIngredient() throws Exception {
		Path args = Source.underWsroot("args");
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Hello", args).instrumentations(instr)
				.nonInstrumentedClasses(junit()).end();

		assertTrue(coverage.ingredients().contains(args));
	}

	public void testDescriptor() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Hello").instrumentations(instr).end();

		assertEquals("net.sf.iwant.api.EmmaCoverage:[" + antJar() + ", "
				+ antLauncherJar() + ", " + emma()
				+ ", instrtest-classes.emma-instr]",
				coverage.contentDescriptor());
	}

	public void testEmmaCoverageProducesTheRequestedEcFile() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Hello");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Hello").instrumentations(instr).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());
	}

	public void testADifferentMainClassCanBeSpecified() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "NotNamedHello");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("NotNamedHello").instrumentations(instr)
				.end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());
	}

	public void testManySourcesForClasses() throws Exception {
		File srcDir = new File(wsRoot, "src1");
		StringBuilder caller = new StringBuilder();
		caller.append("public class Caller {\n");
		caller.append("  public static void main(String[] args) {\n");
		caller.append("    System.out.println(Callee.hello());\n");
		caller.append("  }\n");
		caller.append("}\n");
		Iwant.newTextFile(new File(srcDir, "Caller.java"), caller.toString());

		File srcDir2 = new File(wsRoot, "src2");
		StringBuilder callee = new StringBuilder();
		callee.append("public class Callee {\n");
		callee.append("  public static String hello() {\n");
		callee.append("    return \"Hello\";\n");
		callee.append("  }\n");
		callee.append("}\n");
		Iwant.newTextFile(new File(srcDir2, "Callee.java"), callee.toString());

		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src2");
		JavaClasses classes = JavaClasses.with().name("classes")
				.srcDirs(src1, src2).classLocations().end();
		classes.path(ctx);

		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				classes, Arrays.asList(src1, src2));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Caller").instrumentations(instr).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());
	}

	public void testArgumentsToMainClass() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgPrinter",
				"System.err.println(\"args:\"+java.util.Arrays.toString(args));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("ArgPrinter", "arg1", "arg2")
				.instrumentations(instr).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());

		assertTrue(err().contains("args:[arg1, arg2]\n"));
	}

	public void testArgumentsToMainClassAsPath() throws Exception {
		Target args = new HelloTarget("args",
				"arg1 from file\narg2 from file\n");
		args.path(ctx);

		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "ArgPrinter",
				"System.err.println(\"args:\"+java.util.Arrays.toString(args));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("ArgPrinter", args)
				.instrumentations(instr).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());

		assertTrue(err().contains("args:[arg1 from file, arg2 from file]\n"));
	}

	public void testNonInstrumentedDependency() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "JunitReferrer",
				"System.err.println(\"found \"+Class.forName(\"org.junit.runner.JUnitCore\"));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("JunitReferrer").instrumentations(instr)
				.nonInstrumentedClasses(junit()).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());

		assertTrue(err().contains("found class org.junit.runner.JUnitCore\n"));
	}

	/**
	 * Emma does not instrument interfaces so special handling is needed to
	 * include them in the classpath
	 */
	public void testInterfaceInInstrumentedModule() throws Exception {
		File srcDir = new File(wsRoot, "src");
		StringBuilder caller = new StringBuilder();
		caller.append("public class Impl implements If {\n");
		caller.append("  public static void main(String[] args) {\n");
		caller.append("    System.out.println(new Impl().hello());\n");
		caller.append("  }\n");
		caller.append("  public String hello() {");
		caller.append("    return \"hello\";\n");
		caller.append("  }\n");
		caller.append("}\n");
		Iwant.newTextFile(new File(srcDir, "Impl.java"), caller.toString());

		StringBuilder callee = new StringBuilder();
		callee.append("public interface If {\n");
		callee.append("  String hello();\n");
		callee.append("}\n");
		Iwant.newTextFile(new File(srcDir, "If.java"), callee.toString());

		Path src = Source.underWsroot("src");
		JavaClasses classes = JavaClasses.with().name("classes").srcDirs(src)
				.classLocations().end();
		classes.path(ctx);

		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				classes, Arrays.asList(src));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Impl").instrumentations(instr).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());
	}

	public void testJunitRunOfDynamicallyFoundClassNameList() throws Exception {
		File srcDir = new File(wsRoot, "src");
		StringBuilder aJava = new StringBuilder();
		aJava.append("public class A {\n");
		aJava.append("  public static String hello(String caller) {");
		aJava.append("    System.err.println(\"A called by \"+caller);\n");
		aJava.append("    return \"hello \"+caller;\n");
		aJava.append("  }\n");
		aJava.append("}\n");
		Iwant.newTextFile(new File(srcDir, "A.java"), aJava.toString());

		File testsDir = new File(wsRoot, "tests");
		StringBuilder aTestJava = new StringBuilder();
		aTestJava.append("import static org.junit.Assert.assertEquals;\n");
		aTestJava.append("import org.junit.Test;\n");
		aTestJava.append("public class ATest {\n");
		aTestJava.append("  @Test\n");
		aTestJava.append("  public void a() {\n");
		aTestJava
				.append("    assertEquals(\"hello ATest\", A.hello(\"ATest\"));\n");
		aTestJava.append("  }\n");
		aTestJava.append("}\n");
		Iwant.newTextFile(new File(testsDir, "ATest.java"),
				aTestJava.toString());

		StringBuilder bTestJava = new StringBuilder();
		bTestJava.append("import static org.junit.Assert.assertEquals;\n");
		bTestJava.append("import org.junit.Test;\n");
		bTestJava.append("public class BTest {\n");
		bTestJava.append("  @Test\n");
		bTestJava.append("  public void b() {\n");
		bTestJava
				.append("    assertEquals(\"hello BTest\", A.hello(\"BTest\"));\n");
		bTestJava.append("  }\n");
		bTestJava.append("}\n");
		Iwant.newTextFile(new File(testsDir, "BTest.java"),
				bTestJava.toString());

		Path src = Source.underWsroot("src");
		JavaClasses mainClasses = JavaClasses.with().name("classes")
				.srcDirs(src).classLocations().end();
		mainClasses.path(ctx);
		JavaClasses testClasses = JavaClasses.with().name("test-classes")
				.srcDirs(Source.underWsroot("tests"))
				.classLocations(mainClasses, junit()).end();
		testClasses.path(ctx);

		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				mainClasses, Arrays.asList(src));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		ClassNameList testClassNames = ClassNameList.with()
				.name("test-class-names").classes(testClasses).end();
		testClassNames.path(ctx);

		EmmaCoverage coverage = EmmaCoverage
				.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar())
				.emma(emma())
				.mainClassAndArguments("org.junit.runner.JUnitCore",
						testClassNames).instrumentations(instr)
				.nonInstrumentedClasses(testClasses, junit()).end();
		coverage.path(ctx);

		assertTrue(new File(cacheDir, "instrtest-emma-coverage/coverage.ec")
				.exists());

		assertTrue(err().contains("A called by ATest\n"));
		assertTrue(err().contains("A called by BTest\n"));
	}

	public void testAntScriptUsesGivenJvmArgs() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "Whatever");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with().name("coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Whatever").instrumentations(instr)
				.jvmArgs("-Xmx1024m", "-XX:MaxPermSize=256m").end();
		coverage.path(ctx);

		String xml = testArea
				.contentOf(new File(cacheDir, "coverage/build.xml"));

		assertTrue(xml.contains("<jvmarg value=\"-Xmx1024m\"/>\n"));
		assertTrue(xml.contains("<jvmarg value=\"-XX:MaxPermSize=256m\"/>\n"));
	}

	public void testAllClasspathItemsBothInstrumentedAndNonInstrumentedGoToClasspathInTheSpecifiedOrder()
			throws Exception {
		Path classes1 = Source.underWsroot("classes1");

		JavaClassesAndSources cs1 = newJavaClassesAndSources("one", "One");
		EmmaInstrumentation instr1 = EmmaInstrumentation.of(cs1).using(emma());
		instr1.path(ctx);

		Path classes2 = Source.underWsroot("classes2");

		JavaClassesAndSources cs2 = newJavaClassesAndSources("two", "Two");
		EmmaInstrumentation instr2 = EmmaInstrumentation.of(cs2).using(emma());
		instr2.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("JunitReferrer")
				.nonInstrumentedClasses(classes1).instrumentations(instr1)
				.nonInstrumentedClasses(classes2).instrumentations(instr2)
				.end();

		List<Path> classpath = coverage.classPathIngredients();
		assertEquals(
				"[classes1, one-classes.emma-instr, classes2, two-classes.emma-instr]",
				classpath.toString());
	}

	public void testCoverageBuiltFromJavaSrcModuleUsesModulesDeps()
			throws IOException {
		JavaBinModule bin1 = JavaBinModule
				.providing(Source.underWsroot("bin1"));
		JavaSrcModule src1 = JavaSrcModule.with().name("src1").mainJava("src")
				.mainDeps(bin1).end();
		JavaSrcModule src2 = JavaSrcModule.with().name("src2").mainJava("src")
				.end();
		JavaBinModule bin2 = JavaBinModule
				.providing(Source.underWsroot("bin2"));
		JavaBinModule testLib = JavaBinModule.providing(Source
				.underWsroot("testLib"));

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(src1, bin2, src2).testDeps(testLib)
				.end();

		EmmaCoverage coverage = EmmaCoverage.with()
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("ModTestStuie").module(mod).end();

		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals("[mod-test-classes, testLib,"
				+ " mod-main-classes.emma-instr,"
				+ " src1-main-classes.emma-instr, bin1, bin2,"
				+ " src2-main-classes.emma-instr]", coverage
				.classPathIngredients().toString());
	}

}
