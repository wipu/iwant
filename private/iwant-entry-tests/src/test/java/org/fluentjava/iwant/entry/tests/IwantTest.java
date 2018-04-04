package org.fluentjava.iwant.entry.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.Permission;
import java.util.Arrays;

import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableIwantBootstrapperClassesFromIwantWsRoot;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.iwantwsrootfinder.IwantWsRootFinder;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

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
		testArea = TestArea.forTest(this);
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

	/**
	 * TODO reuse, this is redundant
	 */
	private File mockWsRootZip() {
		try {
			File wsRoot = IwantWsRootFinder.mockWsRoot();
			File zip = new File(testArea.root(), "mock-iwant-wsroot.zip");
			ScriptGenerated.execute(wsRoot.getParentFile(), Arrays.asList("zip",
					"-0", "-q", "-r", zip.getAbsolutePath(), wsRoot.getName()));
			return zip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
				"Usage: org.fluentjava.iwant.entry.Iwant AS_SOMEONE_DIRECTORY [args...]\n",
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
				+ "Please edit and uncomment the properties in it and rerun me.\n",
				err());

		assertEquals("# uncomment and optionally change the commit:\n"
				+ "# (also note the content of the url is assumed unmodifiable so it's downloaded only once)\n"
				+ "#iwant-from=https://github.com/wipu/iwant/archive/"
				+ Iwant.EXAMPLE_COMMIT + ".zip\n",
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
				+ "Please edit and uncomment the properties in it and rerun me.\n",
				err());

		assertEquals("# uncomment and optionally change the commit:\n"
				+ "# (also note the content of the url is assumed unmodifiable so it's downloaded only once)\n"
				+ "#iwant-from=https://github.com/wipu/iwant/archive/"
				+ Iwant.EXAMPLE_COMMIT + ".zip\n",
				testArea.contentOf("as-test/i-have/conf/iwant-from"));
	}

	public void testUserGetsFriendlyErrorIfRerunsIwantWithoutEditingIwantFrom()
			throws Exception {
		testMainCreatesIwantFromAndPrintsHelpIfIHaveDoesNotExist();
		startOfOutAndErrCapture();

		File asSomeone = testArea.newDir("as-test");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("Please define 'iwant-from' in " + asSomeone
				+ "/i-have/conf/iwant-from\nExample:\n"
				+ Iwant.EXAMPLE_IWANT_FROM_CONTENT + "\n", err());
	}

	public void testUserGetsFriendlyErrorIfIwantFromFileDoesNotSpecifyIwantFrom()
			throws Exception {
		testMainCreatesIwantFromAndPrintsHelpIfIHaveDoesNotExist();
		startOfOutAndErrCapture();

		File asSomeone = testArea.newDir("as-test");
		Iwant.newTextFile(new File(asSomeone, "/i-have/conf/iwant-from"),
				"just-something=else\n");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("Please define 'iwant-from' in " + asSomeone
				+ "/i-have/conf/iwant-from\nExample:\n"
				+ Iwant.EXAMPLE_IWANT_FROM_CONTENT + "\n", err());
	}

	public void testUserGetsFriendlyErrorIfIwantFromFileContainsInvalidIwantFromUrl()
			throws Exception {
		testMainCreatesIwantFromAndPrintsHelpIfIHaveDoesNotExist();
		startOfOutAndErrCapture();

		File asSomeone = testArea.newDir("as-test");
		Iwant.newTextFile(new File(asSomeone, "/i-have/conf/iwant-from"),
				"iwant-from=crap\n");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("java.net.MalformedURLException: no protocol: crap\n"
				+ "Please define a valid 'iwant-from' in " + asSomeone
				+ "/i-have/conf/iwant-from\nExample:\n"
				+ Iwant.EXAMPLE_IWANT_FROM_CONTENT + "\n", err());
	}

	public void testIwantSourceZipIsAcquiredWhenItDoesntExist() {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"),
				"iwant-from=" + iwantFromUrl + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");

		assertEquals(cachedIwantZip,
				Iwant.using(network).iwantSourceZipOfWishedVersion(asSomeone));
		assertEquals(testArea.contentOf(iwantZip),
				testArea.contentOf(cachedIwantZip));
	}

	public void testIwantSourceIsAcquiredWhenItDoesntExist() {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"),
				"iwant-from=" + iwantFromUrl + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");

		File cachedIwantSrc = Iwant.using(network)
				.iwantSourceOfWishedVersion(asSomeone);

		// the src shall be the sole child of the unzipped source zip:
		assertEquals(new File(cachedIwantZipUnzipped, "iwant-mock-wsroot"),
				cachedIwantSrc);
		// and it shall contain files from the mock wsroot
		assertTrue(new File(cachedIwantSrc, "essential/iwant-entry").exists());
		assertTrue(
				new File(cachedIwantSrc, "essential/iwant-api-bash").exists());
	}

	public void testIwantBootstrapsWhenNothingHasBeenDownloadedAndJustIwantFromFileIsGiven()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"),
				"iwant-from=" + iwantFromUrl + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");
		File cachedIwantEssential = new File(cachedIwantZipUnzipped,
				"iwant-mock-wsroot/essential");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						cachedIwantEssential),
				"iwant-bootstrapper-classes");

		Iwant.using(network).evaluate(asSomeone.getCanonicalPath(), "args",
				"for", "entry two");

		String cwd = System.getProperty("user.dir");
		assertEquals("Mocked iwant entry2\n" + "CWD: " + cwd + "\n" + "args: ["
				+ cachedIwantZipUnzipped + "/iwant-mock-wsroot/essential, "
				+ asSomeone + ", args, for, entry two]\n"
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
		Iwant.mkdirs(dir);
		UnmodifiableIwantBootstrapperClassesFromIwantWsRoot with = new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
				dir);
		assertEquals(with.location().toExternalForm(),
				without.location().toExternalForm());
	}

	/**
	 * This happens when JRE is used instead of JDK
	 */
	public void testIwantGivesNiceErrorMessageIfSystemJavaCompilerIsNotFound()
			throws Exception {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"),
				"iwant-from=" + iwantFromUrl + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");
		File cachedIwantEssential = new File(cachedIwantZipUnzipped,
				"iwant-mock-wsroot/essential");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						cachedIwantEssential),
				"iwant-bootstrapper-classes");

		network.shallNotFindSystemJavaCompiler();

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

	public void testBootstrapperIsNotCompiledIfNotNecessary() throws Exception {
		File asSomeone = testArea.newDir("as-test");
		File iHaveConf = testArea.newDir("as-test/i-have/conf");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		Iwant.newTextFile(new File(iHaveConf, "iwant-from"),
				"iwant-from=" + iwantFromUrl + "\n");

		IwantNetworkMock network = new IwantNetworkMock(testArea);
		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");
		File cachedIwantEssential = new File(cachedIwantZipUnzipped,
				"iwant-mock-wsroot/essential");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						cachedIwantEssential),
				"iwant-bootstrapper-classes");

		Iwant.using(network).evaluate(asSomeone.getCanonicalPath(), "args",
				"for", "entry two");

		String cwd = System.getProperty("user.dir");
		assertEquals("Mocked iwant entry2\n" + "CWD: " + cwd + "\n" + "args: ["
				+ cachedIwantZipUnzipped + "/iwant-mock-wsroot/essential, "
				+ asSomeone + ", args, for, entry two]\n"
				+ "And hello from mocked entry one.\n", out());

		File bsClasses = new File(testArea.root(),
				"iwant-bootstrapper-classes");
		long t1 = bsClasses.lastModified();
		Iwant.using(network).evaluate(asSomeone.getCanonicalPath(), "new args",
				"for", "entry two");
		long t2 = bsClasses.lastModified();

		assertEquals(t1, t2);
	}

	public void testMkdirsCreatesDirectoryWithParent() {
		File dir = new File(testArea.root(), "a/b");
		Iwant.mkdirs(dir);

		assertTrue(dir.exists());
		assertTrue(dir.isDirectory());
	}

	public void testMkdirsIsOkForExistentDirectory() {
		File dir = new File(testArea.root(), "a/b");
		Iwant.mkdirs(dir);
		Iwant.mkdirs(dir);

		assertTrue(dir.exists());
		assertTrue(dir.isDirectory());
	}

	public void testMkdirsThrowsAndRefusesToTouchExistingNonDir() {
		File nondir = testArea.hasFile("nondir", "anything");

		try {
			Iwant.mkdirs(nondir);
			fail();
		} catch (IwantException e) {
			assertEquals("mkdirs failed for existing non-directory " + nondir,
					e.getMessage());
			assertEquals("anything", testArea.contentOf(nondir));
		}
	}

	public void testMkdirsThrowsIfNoPermissions() {
		File parent = testArea.newDir("parent");
		File dir = new File(parent, "a/b");

		try {
			parent.setWritable(false);

			Iwant.mkdirs(dir);

			fail();
		} catch (IwantException e) {
			assertEquals("mkdirs failed for " + dir, e.getMessage());
			assertFalse(dir.exists());
		} finally {
			parent.setWritable(true);
		}
	}

	public void testDelDeletesEvenANonEmptyDirectory() {
		File dirToDelete = testArea.newDir("dir");
		testArea.hasFile("dir/file", "anything");

		Iwant.del(dirToDelete);

		assertFalse(dirToDelete.exists());
	}

	public void testDelDoesNotComplainAboutNonexistentFile() {
		File nonexistent = new File(testArea.root(), "nonexistent");
		assertFalse(nonexistent.exists());

		Iwant.del(nonexistent);

		assertFalse(nonexistent.exists());
	}

	public void testDelThrowsIfNoPermissions() {
		File parent = testArea.newDir("parent");
		File fileToDelete = new File(parent, "file");
		testArea.fileHasContent(fileToDelete, "anything");

		try {
			parent.setWritable(false);

			Iwant.del(fileToDelete);

			fail();
		} catch (IwantException e) {
			assertEquals("del failed for " + fileToDelete, e.getMessage());
			assertTrue(fileToDelete.exists());
		} finally {
			parent.setWritable(true);
		}
	}

	public void testDelWorksEvenWithBrokenSymlinkUnderDirToDelete()
			throws IOException, InterruptedException {
		File parent = testArea.newDir("parent");
		ScriptGenerated.execute(parent, Arrays.asList("ln", "-s",
				"/nonexistent/symlink/target", "broken-symlink"));

		Iwant.del(parent);

		assertFalse(parent.exists());
	}

}
