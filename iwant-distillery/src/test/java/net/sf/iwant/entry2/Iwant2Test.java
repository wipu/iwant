package net.sf.iwant.entry2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry2.Iwant2.ClassesFromUnmodifiableIwantWsRoot;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantEntry2TestArea;
import net.sf.iwant.testing.IwantNetworkMock;
import net.sf.iwant.testing.WsRootFinder;

public class Iwant2Test extends TestCase {

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private TestArea testArea;

	private SecurityManager origSecman;

	private InputStream originalIn;

	private PrintStream originalOut;

	private PrintStream originalErr;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	private String originalLineSeparator;

	private IwantNetworkMock network;
	private Iwant2 iwant2;

	/**
	 * TODO a reusable main-method testing tools project
	 */
	@Override
	public void setUp() {
		origSecman = System.getSecurityManager();
		System.setSecurityManager(new ExitCatcher());
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
		testArea = new IwantEntry2TestArea();
		network = new IwantNetworkMock(testArea);
		iwant2 = Iwant2.using(network);
	}

	private void startOfOutAndErrCapture() {
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	private static class ExitCalledException extends SecurityException {

		private final int status;

		public ExitCalledException(int status) {
			this.status = status;
		}

		@SuppressWarnings("unused")
		public int status() {
			return status;
		}

	}

	private static class ExitCatcher extends SecurityManager {

		@Override
		public void checkPermission(Permission perm) {
			// everything allowed
		}

		@Override
		public void checkExit(int status) {
			throw new ExitCalledException(status);
		}

	}

	@Override
	public void tearDown() {
		System.setSecurityManager(origSecman);
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
		System.err.print("== out:\n" + out());
		System.err.print("== err:\n" + err());

		if (!out().isEmpty()) {
			System.err.println("=== out:\n" + out());
		}
		if (!err().isEmpty()) {
			System.err.println("=== err:\n" + err());
		}
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	private static String mockedIwantTestRunner() {
		StringBuilder b = new StringBuilder();
		b.append("package net.sf.iwant.testrunner;\n");
		b.append("\n");
		b.append("import java.util.Arrays;\n");
		b.append("\n");
		b.append("public class IwantTestRunner {\n");
		b.append("\n");
		b.append("	public static void main(String[] args) {\n");
		b.append("		System.out.println(\"Mocked IwantTestRunner\");\n");
		b.append("		System.out.println(\"args: \" + Arrays.toString(args));\n");
		b.append("	}\n");
		b.append("\n");
		b.append("}\n");
		return b.toString();
	}

	private static String mockedFailingIwantTestRunner() {
		StringBuilder b = new StringBuilder();
		b.append("package net.sf.iwant.testrunner;\n");
		b.append("\n");
		b.append("public class IwantTestRunner {\n");
		b.append("\n");
		b.append("  public static void main(String[] args) {\n");
		b.append("    throw new IllegalStateException(\"Simulated failure.\");");
		b.append("  }\n");
		b.append("\n");
		b.append("}\n");
		return b.toString();
	}

	private static File mockedIwantRunnerJava() {
		return new File(WsRootFinder.mockWsRoot(),
				"iwant-testrunner/src/main/java/net/sf/iwant/testrunner/IwantTestRunner.java");
	}

	private static void mockedIwantRunnerShallSucceed() {
		Iwant.newTextFile(mockedIwantRunnerJava(), mockedIwantTestRunner());
	}

	private static void mockedIwantRunnerShallFail() {
		Iwant.newTextFile(mockedIwantRunnerJava(),
				mockedFailingIwantTestRunner());
	}

	public void testIwant2CompilesIwantAndRunsTestSuiteAndFailsWhenItFails()
			throws Exception {
		File iwantWsRoot = WsRootFinder.mockWsRoot();
		network.usesRealJunitUrlAndCached();
		network.cachesAt(new ClassesFromUnmodifiableIwantWsRoot(iwantWsRoot),
				"all-iwant-classes");
		mockedIwantRunnerShallFail();

		try {
			iwant2.evaluate(iwantWsRoot, "args", "to be", "passed");
			fail();
		} catch (InvocationTargetException e) {
			System.err.println(e);
			IllegalStateException ise = (IllegalStateException) e.getCause();
			assertEquals("Simulated failure.", ise.getMessage());
		}

		assertEquals("", out());
	}

	public void testIwant2CompilesIwantAndCallsIwant3AfterTestSuiteSuccess()
			throws Exception {
		File iwantWsRoot = WsRootFinder.mockWsRoot();
		network.usesRealJunitUrlAndCached();
		network.cachesAt(new ClassesFromUnmodifiableIwantWsRoot(iwantWsRoot),
				"all-iwant-classes");
		mockedIwantRunnerShallSucceed();

		iwant2.evaluate(WsRootFinder.mockWsRoot(), "args", "to be", "passed");

		assertEquals("Mocked net.sf.iwant.entry3.Iwant3\n" + "args: ["
				+ iwantWsRoot + ", args, to be, passed]\n", out());
		// we cannot assert mocked IwantTestRunner output, because we catch it
		// (real junit is too chatty)
	}

	public void testIwant2CompilesIwantWithDebugInformation() throws Exception {
		File iwantWsRoot = WsRootFinder.mockWsRoot();
		network.usesRealJunitUrlAndCached();
		network.cachesAt(new ClassesFromUnmodifiableIwantWsRoot(iwantWsRoot),
				"all-iwant-classes");
		mockedIwantRunnerShallSucceed();

		iwant2.evaluate(WsRootFinder.mockWsRoot());

		File iwantClasses = new File(testArea.root(), "all-iwant-classes");
		File classWithVars = new File(iwantClasses,
				"net/sf/iwant/api/ClassToTestDebugInformation.class");

		// TODO reuse code with JavaClassesTest to read as bytes. This is not
		// robust because we parse binary as String
		String contentOfClass = Iwant2.contentAsString(classWithVars);
		assertTrue(contentOfClass.contains("parameterVariable"));
		assertTrue(contentOfClass.contains("localVariable"));
	}

	public void testFindingJavaFilesUnderSrcDirThatContainsNonJavaAndSvnMetadata() {
		File src = testArea.newDir("src");
		new File(src, ".svn").mkdirs();
		File aJava = Iwant.newTextFile(new File(src, "A.java"), "");

		File pak1 = new File(src, "pak1");
		new File(pak1, ".svn").mkdirs();
		File bJava = Iwant.newTextFile(new File(pak1, "B.java"), "");
		Iwant.newTextFile(new File(pak1, "crap.notjava"), "");

		assertEquals("[" + aJava + ", " + bJava + "]", Iwant2
				.javaFilesRecursivelyUnder(src).toString());
	}

}
