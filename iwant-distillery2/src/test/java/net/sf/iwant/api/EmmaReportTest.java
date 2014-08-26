package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EmmaReportTest extends TestCase {

	private TestArea testArea;
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
		testArea = TestArea.forTest(this);
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

	public void testIngredientsAndDescriptor() throws Exception {
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				Source.underWsroot("classes"), Source.underWsroot("src"));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Whatever").instrumentations(instr)
				.end();

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages(coverage).end();

		assertEquals("[" + emma()
				+ ", classes.emma-instr, instrtest-emma-coverage]", report
				.ingredients().toString());
		assertEquals("net.sf.iwant.api.EmmaReport:[" + emma()
				+ ", classes.emma-instr, instrtest-emma-coverage]",
				report.contentDescriptor());

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

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages(coverage).end();
		report.path(ctx);

		assertTrue(new File(ctx.cached(report), "coverage.txt").exists());
		assertTrue(new File(ctx.cached(report), "coverage/index.html").exists());
		assertTrue(new File(ctx.cached(report), "coverage.xml").exists());
	}

	public void testFilteredOutModuleDoesNotBreakReportBuilding()
			throws Exception {
		String filterFileString = "emma-filter.txt";
		Iwant.newTextFile(new File(wsRoot, filterFileString), "-Filtered\n");
		Path filter = Source.underWsroot(filterFileString);

		JavaClassesAndSources cs1 = newJavaClassesAndSources("filtered",
				"Filtered");
		EmmaInstrumentation instr1 = EmmaInstrumentation.of(cs1).filter(filter)
				.using(emma());
		instr1.path(ctx);

		JavaClassesAndSources cs2 = newJavaClassesAndSources("nonfiltered",
				"NonFiltered");
		EmmaInstrumentation instr2 = EmmaInstrumentation.of(cs2).filter(filter)
				.using(emma());
		instr2.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("NonFiltered")
				.instrumentations(instr1, instr2).end();
		coverage.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr1, instr2).coverages(coverage).end();
		report.path(ctx);

		assertTrue(new File(ctx.cached(report), "coverage.txt").exists());
		assertTrue(new File(ctx.cached(report), "coverage/index.html").exists());
		assertTrue(new File(ctx.cached(report), "coverage.xml").exists());
	}

	public void testEmptyReportDirectoryIsGeneratedWhenNoCoveragesAreGiven()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "JunitReferrer",
				"System.err.println(\"found \"+Class.forName(\"org.junit.runner.JUnitCore\"));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages().end();
		report.path(ctx);

		assertTrue(ctx.cached(report).exists());
		assertFalse(new File(ctx.cached(report), "coverage.txt").exists());
		assertFalse(new File(ctx.cached(report), "coverage/index.html")
				.exists());
		assertFalse(new File(ctx.cached(report), "coverage.xml").exists());
	}

}
