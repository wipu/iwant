package net.sf.iwant.entry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.Permission;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.UnmodifiableIwantBootstrapperClassesFromIwantWsRoot;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantEntryTestArea;
import net.sf.iwant.testing.IwantNetworkMock;
import net.sf.iwant.testing.WsRootFinder;

public class IwantTest extends TestCase {

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private TestArea testArea;

	private SecurityManager origSecman;

	private InputStream originalIn;

	private PrintStream originalOut;

	private PrintStream originalErr;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	private String originalLineSeparator;

	/**
	 * TODO a reusable main-method testing tools project
	 */
	@Override
	public void setUp() {
		testArea = new IwantEntryTestArea();
		origSecman = System.getSecurityManager();
		System.setSecurityManager(new ExitCatcher());
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
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

	public void testMainFailsAndExitsIfGivenZeroArguments() throws Exception {
		try {
			Iwant.main(new String[] {});
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals(
				"Usage: net.sf.iwant.entry.Iwant AS_SOMEONE_DIRECTORY [args...]\n",
				err());
	}

	public void testMainFailsAndExitsIfGivenAsSomeoneDoesNotExist()
			throws Exception {
		try {
			Iwant.main(new String[] { testArea.root() + "/as-missing" });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("AS_SOMEONE_DIRECTORY does not exist: " + testArea.root()
				+ "/as-missing\n", err());
	}

	public void testMainCreatesIwantFromAndPrintsHelpIfIHaveDoesNotExist()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("I created " + asSomeone + "/i-have/conf/iwant-from\n"
				+ "Please edit it and rerun me.\n", err());

		assertEquals("iwant-from=TODO\n",
				testArea.contentOf("as-test/i-have/conf/iwant-from"));
	}

	public void testMainCreatesIwantFromAndPrintsHelpIfIwantFromDoesNotExist()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		testArea.newDir("as-test/i-have");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("I created " + asSomeone + "/i-have/conf/iwant-from\n"
				+ "Please edit it and rerun me.\n", err());

		assertEquals("iwant-from=TODO\n",
				testArea.contentOf("as-test/i-have/conf/iwant-from"));
	}

	public void testIwantIsSvnExportedWhenNotExported() {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		URL iwantFrom = Iwant.fileToUrl(WsRootFinder.mockWsRoot());
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"), "iwant-from="
				+ iwantFrom + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		File exportedWsRoot = network.cachesUrlAt(iwantFrom,
				"exported-iwant-wsroot");

		File exportedWsRootAgain = Iwant.using(network)
				.iwantWsrootOfWishedVersion(asSomeone);
		assertEquals(exportedWsRoot, exportedWsRootAgain);
		assertTrue(new File(exportedWsRoot, "iwant-api-model").exists());
	}

	/**
	 * See the svn export test
	 */
	public void testExistingIwantIsSvnReExportedFromFile() {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		URL iwantFrom = Iwant.fileToUrl(WsRootFinder.mockWsRoot());
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"), "iwant-from="
				+ iwantFrom + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		File exportedWsRoot = network.cachesUrlAt(iwantFrom,
				"exported-iwant-wsroot");
		File preExisting = new File(exportedWsRoot, "existing");
		preExisting.mkdirs();

		File exportedWsRootAgain = Iwant.using(network)
				.iwantWsrootOfWishedVersion(asSomeone);
		assertEquals(exportedWsRoot, exportedWsRootAgain);

		assertTrue(new File(exportedWsRoot, "iwant-api-model").exists());
		assertFalse(preExisting.exists());
	}

	/**
	 * See the svn export test
	 */
	public void testExistingIwantIsNotSvnReExportedFromFileWhenToldNotTo() {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		URL iwantFrom = Iwant.fileToUrl(WsRootFinder.mockWsRoot());
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"), "iwant-from="
				+ iwantFrom + "\nre-export=false\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		File exportedWsRoot = network.cachesUrlAt(iwantFrom,
				"exported-iwant-wsroot");
		File preExisting = new File(exportedWsRoot, "existing");
		preExisting.mkdirs();

		File exportedWsRootAgain = Iwant.using(network)
				.iwantWsrootOfWishedVersion(asSomeone);
		assertEquals(exportedWsRoot, exportedWsRootAgain);

		assertFalse(new File(exportedWsRoot, "iwant-testrunner").exists());
		assertTrue(preExisting.exists());
	}

	public void testIwantBootstrapsWhenNothingHasBeenDownloadedAndJustIwantFromIsGiven()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		URL iwantFrom = Iwant.fileToUrl(WsRootFinder.mockWsRoot());
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"), "iwant-from="
				+ iwantFrom + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		File exportedWsRoot = network.cachesUrlAt(iwantFrom,
				"exported-iwant-wsroot");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						exportedWsRoot), "iwant-bootstrapper-classes");

		Iwant.using(network).evaluate(asSomeone.getCanonicalPath(), "args",
				"for", "entry two");

		String cwd = System.getProperty("user.dir");
		assertEquals("Mocked iwant entry2\n" + "CWD: " + cwd + "\n" + "args: ["
				+ exportedWsRoot + ", " + asSomeone
				+ ", args, for, entry two]\n"
				+ "And hello from mocked entry one.\n", out());
		assertTrue(err().endsWith("And syserr message from mocked entry2\n"));
	}

	public void testTrailingSlashRemoval() {
		assertEquals("", Iwant.withoutTrailingSlash(""));
		assertEquals("", Iwant.withoutTrailingSlash("/"));
		assertEquals("a", Iwant.withoutTrailingSlash("a"));
		assertEquals("a", Iwant.withoutTrailingSlash("a/"));
		assertEquals("file://a/b", Iwant.withoutTrailingSlash("file://a/b"));
		assertEquals("file://a/b", Iwant.withoutTrailingSlash("file://a/b/"));
	}

	/**
	 * File to uri to url ends with slash iff dir exists so it's too random for
	 * us
	 */
	public void testFileToUrlNeverEndsInSlashRegardlessOfExistenceOfFile() {
		File dir = new File(testArea.root(), "dir");
		UnmodifiableIwantBootstrapperClassesFromIwantWsRoot without = new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
				dir);
		dir.mkdir();
		UnmodifiableIwantBootstrapperClassesFromIwantWsRoot with = new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
				dir);
		assertEquals(with.location().toExternalForm(), without.location()
				.toExternalForm());
	}

	/**
	 * This happens when JRE is used instead of JDK
	 */
	public void testIwantGivesNiceErrorMessageIfSystemJavaCompilerIsNotFound()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		URL iwantFrom = Iwant.fileToUrl(WsRootFinder.mockWsRoot());
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"), "iwant-from="
				+ iwantFrom + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.shallNotFindSystemJavaCompiler();

		File exportedWsRoot = network.cachesUrlAt(iwantFrom,
				"exported-iwant-wsroot");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						exportedWsRoot), "iwant-bootstrapper-classes");

		try {
			Iwant.using(network).evaluate(asSomeone.getCanonicalPath(), "args",
					"for", "entry two");
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Cannot find system java compiler. Are you running a JRE instead of JDK?",
					e.getMessage());
		}
	}

}
