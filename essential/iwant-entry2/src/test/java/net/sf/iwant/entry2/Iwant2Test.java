package net.sf.iwant.entry2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry2.Iwant2.ClassesFromUnmodifiableIwantEssential;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.testarea.TestArea;

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
		testArea = TestArea.forTest(this);
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
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	public void testIwant2CompilesIwantAndCallsIwant3() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(new ClassesFromUnmodifiableIwantEssential(
				iwantEssential), "all-iwant-classes");

		iwant2.evaluate(iwantEssential, "args", "to be", "passed");

		assertEquals(":        compiled -> all-iwant-classes\n", err());
		assertEquals("Mocked net.sf.iwant.entry3.Iwant3\n" + "args: ["
				+ iwantEssential + ", args, to be, passed]\n", out());
	}

	public void testIwant2CallsIwant3UsingCorrectClasspath() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(new ClassesFromUnmodifiableIwantEssential(
				iwantEssential), "all-iwant-classes");

		iwant2.evaluate(iwantEssential, "--printClassLoaderUrls");
		assertEquals(":        compiled -> all-iwant-classes\n", err());
		assertEquals("Mocked net.sf.iwant.entry3.Iwant3\n" + "args: ["
				+ iwantEssential + ", --printClassLoaderUrls]\n"
				+ "classloader urls: [file:" + iwantEssential
				+ "/iwant-wsroot-marker/, file:" + testArea.root()
				+ "/all-iwant-classes/]\n", out());
	}

	public void testIwant2CompilesIwantWithDebugInformation() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(new ClassesFromUnmodifiableIwantEssential(
				iwantEssential), "all-iwant-classes");

		iwant2.evaluate(iwantEssential);

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
