package org.fluentjava.iwant.entry2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry2.Iwant2.ClassesFromUnmodifiableIwantEssential;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.iwantwsrootfinder.IwantWsRootFinder;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Iwant2Test {

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

	private File antJar;

	private File antLauncherJar;

	/**
	 * TODO a reusable main-method testing tools project
	 */
	@BeforeEach
	public void before() {
		assertRealIwant3IsNotInClasspath();
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
		useRealAntJars(network);
		iwant2 = Iwant2.using(network);
	}

	/**
	 * Here we test delegation to the "mocked" Iwant3 from iwant-mock-wsroot.
	 * OTOH it would be cleaner to just configure Iwant2 to call another FQCN
	 * but, since iwant gives controle over the exact classpath, we'll simply
	 * guard against accidental breaks with this.
	 */
	private static void assertRealIwant3IsNotInClasspath() {
		try {
			Class.forName("org.fluentjava.iwant.entry3.Iwant3");
			fail("Cannot proceed, *real* Iwant3 was found in the classpath");
		} catch (ClassNotFoundException e) {
			// expected
		}
	}

	private void useRealAntJars(IwantNetworkMock network) {
		Iwant2 iw2 = new Iwant2(Iwant.usingRealNetwork().network());
		antJar = iw2.antJar();
		network.cachesUrlAt(iw2.antJarUrl(), antJar);
		antLauncherJar = iw2.antLauncherJar();
		network.cachesUrlAt(iw2.antLauncherJarUrl(), antLauncherJar);
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

	@AfterEach
	public void after() {
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

	@Test
	public void iwant2CompilesIwantAndCallsIwant3() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(
				new ClassesFromUnmodifiableIwantEssential(iwantEssential),
				"all-iwant-classes");

		iwant2.evaluate(iwantEssential, "args", "to be", "passed");

		assertEquals(":       Compiling -> " + testArea.root()
				+ "/all-iwant-classes\n", err());
		assertEquals("Mocked org.fluentjava.iwant.entry3.Iwant3\n" + "args: ["
				+ iwantEssential + ", args, to be, passed]\n", out());
	}

	@Test
	public void iwant2CallsIwant3UsingCorrectClasspath() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(
				new ClassesFromUnmodifiableIwantEssential(iwantEssential),
				"all-iwant-classes");

		iwant2.evaluate(iwantEssential, "--printClassLoaderUrls");
		assertEquals(":       Compiling -> " + testArea.root()
				+ "/all-iwant-classes\n", err());
		assertEquals("Mocked org.fluentjava.iwant.entry3.Iwant3\n" + "args: ["
				+ iwantEssential + ", --printClassLoaderUrls]\n"
				+ "classloader urls: [file:" + iwantEssential
				+ "/iwant-wsroot-marker/, file:" + testArea.root()
				+ "/all-iwant-classes/, " + antJar.toURI() + ", "
				+ antLauncherJar.toURI() + "]\n", out());

		// all essential module main sources can be found in the combined
		// essentials:
		// TODO assert some more, perhaps
		testArea.shallContainFragmentIn("all-iwant-classes/"
				+ "org/fluentjava/iwant/embedded/MockedAsEmbeddedIwantUser.class",
				"MockedAsEmbeddedIwantUser");
	}

	@Test
	public void iwant2CompilesIwantWithDebugInformation() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(
				new ClassesFromUnmodifiableIwantEssential(iwantEssential),
				"all-iwant-classes");

		iwant2.evaluate(iwantEssential);

		File iwantClasses = new File(testArea.root(), "all-iwant-classes");
		File classWithVars = new File(iwantClasses,
				"org/fluentjava/iwant/api/ClassToTestDebugInformation.class");

		// TODO reuse code with JavaClassesTest to read as bytes. This is not
		// robust because we parse binary as String
		String contentOfClass = Iwant2.contentAsString(classWithVars);
		assertTrue(contentOfClass.contains("parameterVariable"));
		assertTrue(contentOfClass.contains("localVariable"));
	}

	@Test
	public void findingJavaFilesUnderSrcDirThatContainsNonJavaAndSvnMetadata() {
		File src = testArea.newDir("src");
		Iwant.mkdirs(new File(src, ".svn"));
		File aJava = Iwant.newTextFile(new File(src, "A.java"), "");

		File pak1 = new File(src, "pak1");
		Iwant.mkdirs(new File(pak1, ".svn"));
		File bJava = Iwant.newTextFile(new File(pak1, "B.java"), "");
		Iwant.newTextFile(new File(pak1, "crap.notjava"), "");

		assertEquals("[" + aJava + ", " + bJava + "]",
				Iwant2.javaFilesRecursivelyUnder(src).toString());
	}

	@Test
	public void iwant2CopiesApiBashResourcesToIwantClasses() throws Exception {
		File iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(
				new ClassesFromUnmodifiableIwantEssential(iwantEssential),
				"all-iwant-classes");

		iwant2.evaluate(iwantEssential);

		assertEquals("represents api bash resources\n", testArea.contentOf(
				"all-iwant-classes/org/fluentjava/iwant/api/bash/mock-resource.txt"));
	}

}
