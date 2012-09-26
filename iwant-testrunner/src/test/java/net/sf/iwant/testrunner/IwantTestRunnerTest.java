package net.sf.iwant.testrunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class IwantTestRunnerTest extends TestCase {

	private PrintStream origOut;
	private ByteArrayOutputStream out;

	@Override
	public void setUp() {
		out = new ByteArrayOutputStream();
		origOut = System.out;
		System.setOut(new PrintStream(out));
	}

	@Override
	public void tearDown() {
		System.setOut(origOut);
	}

	public void testRuntimeExceptionIsThrownWhenSuiteFails() throws Exception {
		try {
			IwantTestRunner.main(new String[] { Failing.class
					.getCanonicalName() });
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Test failed.", e.getMessage());
			// TODO use lineseparator to make it work in Wintoys:
			assertTrue(out
					.toString()
					.contains(
							"There was 1 failure:\n"
									+ "1) testFailure(net.sf.iwant.testrunner.Failing$Tst)"
									+ "junit.framework.AssertionFailedError:"
									+ " Simulated test failure to test IwantTestRunner."));
		}
	}

	public void testNoExceptionIsThrownWhenSuiteIsSuccessful() throws Exception {
		assertFalse(Succeeding.Tst.wasRun());
		IwantTestRunner
				.main(new String[] { Succeeding.class.getCanonicalName() });
		assertTrue(Succeeding.Tst.wasRun());
	}

}
