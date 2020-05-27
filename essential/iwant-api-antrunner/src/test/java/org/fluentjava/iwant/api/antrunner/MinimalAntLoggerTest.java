package org.fluentjava.iwant.api.antrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MinimalAntLoggerTest {

	private MinimalAntLogger logger;
	private ByteArrayOutputStream errBytes;
	private PrintStream err;
	private Project project;

	@Before
	public void before() {
		logger = new MinimalAntLogger();
		errBytes = new ByteArrayOutputStream();
		err = new PrintStream(errBytes);
		logger.setErrorPrintStream(err);
		project = new Project();
	}

	@After
	public void after() {
		err.close();
	}

	private BuildEvent eventWithMessage(String message) {
		BuildEvent ev = new BuildEvent(project);
		ev.setMessage(message, 0);
		return ev;
	}

	@Test
	public void nothingIsPrintedForBuildEventWithoutMessage() {
		logger.messageLogged(eventWithMessage(null));
		logger.messageLogged(eventWithMessage(""));
		logger.messageLogged(eventWithMessage("   "));

		err.flush();

		assertEquals(0, errBytes.toByteArray().length);
	}

	@Test
	public void nonEmptyEventMessageIsPrinted() {
		logger.messageLogged(eventWithMessage("hello from ant"));

		err.flush();

		assertEquals("[ant]   hello from ant\n", errBytes.toString());
	}

	@Test
	public void failureIsPrintedEvenIfEventHasNoMessage() {
		BuildEvent msg = eventWithMessage(null);
		msg.setException(new Exception("failure from ant"));
		logger.messageLogged(msg);

		err.flush();

		assertTrue(errBytes.toString()
				.startsWith("java.lang.Exception: failure from ant\n" + "	"));
	}

}
