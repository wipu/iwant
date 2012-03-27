package net.sf.iwant.entry2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.Permission;

import junit.framework.TestCase;
import net.sf.iwant.entry.IwantEntryTestArea;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry.WsRootFinder;
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
	public void setUp() {
		origSecman = System.getSecurityManager();
		System.setSecurityManager(new ExitCatcher());
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
		testArea = new IwantEntryTestArea();
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
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	public void testIwant2CompilesAndCallsIwant3() {
		iwant2.evaluate(WsRootFinder.mockWsRoot(), "args", "to be", "passed");

		assertEquals("Mocked net.sf.iwant.entry3.Iwant3\n"
				+ "args: [args, to be, passed]\n", out());
		assertTrue(err().contains(" javac "));
		assertTrue(err().contains(" invoke "));
	}

}
