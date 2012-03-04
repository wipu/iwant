package net.sf.iwant.entry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

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
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	public void testMainFailsAndExitsIfGivenZeroArguments() {
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

	public void testMainFailsAndExitsIfGivenAsSomeoneDoesNotExist() {
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
			throws IOException {
		File asSomeone = testArea.newDir("as-test");
		try {
			Iwant.main(new String[] { asSomeone.getCanonicalPath() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}
		assertEquals("", out());
		assertEquals("I created " + asSomeone + "/i-have/iwant-from\n"
				+ "Please edit it and rerun me.\n", err());

		assertEquals("iwant-from=TODO\n",
				testArea.contentOf("as-test/i-have/iwant-from"));
	}

}
