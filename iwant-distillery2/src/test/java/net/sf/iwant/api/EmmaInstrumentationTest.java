package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class EmmaInstrumentationTest extends TestCase {

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
	private File cwd;

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
		cwd = new File(System.getProperty("user.dir"));
	}

	@Override
	protected void tearDown() throws Exception {
		System.setErr(oldErr);
		System.setOut(oldOut);
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path emma() throws IOException {
		return downloaded(TestedIwantDependencies.emma());
	}

	private JavaClassesAndSources newJavaClassesAndSources(String name)
			throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);
		Iwant.newTextFile(new File(srcDir, "Hello.java"),
				"public class Hello {\n"
						+ "  public static void main(String[] args) {\n"
						+ "    System.out.println(\"main\");\n" + "  }\n}\n");
		JavaClasses classes = new JavaClasses(name + "-classes",
				Source.underWsroot(srcDirString),
				Collections.<Path> emptyList());
		classes.path(ctx);
		return new JavaClassesAndSources(classes,
				Source.underWsroot(srcDirString));
	}

	// the tests

	public void testNameIsDerivedFromTheNameOfJavaClassesAndSourcesPair()
			throws IOException {
		assertEquals(
				"one.emma-instr",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).name());
		assertEquals(
				"two.emma-instr",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).name());
	}

	public void testIngredientsAreEmmaAndTheClasses() throws IOException {
		assertEquals(
				"[" + emma() + ", one]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).ingredients().toString());
		assertEquals(
				"[" + emma() + ", two]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).ingredients().toString());
	}

	public void testDescriptor() throws IOException {
		assertEquals(
				"net.sf.iwant.api.EmmaInstrumentation:[" + emma() + ", one]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("one"), Source
										.underWsroot("irrelevant")))
						.using(emma()).contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.EmmaInstrumentation:[" + emma() + ", two]",
				EmmaInstrumentation
						.of(new JavaClassesAndSources(
								Source.underWsroot("two"), Source
										.underWsroot("irrelevant")))
						.using(emma()).contentDescriptor());
	}

	public void testInstrumentationCreatesNeededFiles() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources("instrtest");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());

		instr.path(ctx);

		File instrDir = new File(cacheDir, "instrtest-classes.emma-instr");

		assertEquals("metadata.out.file=" + instrDir + "/emma.em\n"
				+ "verbosity.level=warning\n" + "coverage.out.file=" + instrDir
				+ "/please-override-when-running-tests.ec\n",
				testArea.contentOf(new File(instrDir, "emma-instr.properties")));
		assertTrue(new File(instrDir, "emma.em").exists());
		assertTrue(new File(instrDir, "instr-classes").exists());
	}

	/**
	 * If property overrides don't work, the metafile gets created to cwd
	 */
	public void testInstrumentationDoesNotCreateMetafileToCwd()
			throws Exception {
		testInstrumentationCreatesNeededFiles();
		assertFalse(new File(cwd, "coverage.em").exists());

	}

}
