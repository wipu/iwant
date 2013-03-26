package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import junit.framework.TestCase;
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

}
