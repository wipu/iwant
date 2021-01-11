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

	private BuildEvent event(int priority, String message) {
		BuildEvent ev = new BuildEvent(project);
		ev.setMessage(message, priority);
		return ev;
	}

	@Test
	public void nothingIsPrintedForBuildEventWithoutMessage() {
		logger.messageLogged(event(0, null));
		logger.messageLogged(event(0, ""));
		logger.messageLogged(event(0, "   "));

		err.flush();

		assertEquals(0, errBytes.toByteArray().length);
	}

	@Test
	public void nonEmptyEventMessageIsPrinted() {
		logger.messageLogged(event(0, "hello from ant"));

		err.flush();

		assertEquals("[ant]   hello from ant\n", errBytes.toString());
	}

	@Test
	public void failureIsPrintedEvenIfEventHasNoMessage() {
		BuildEvent msg = event(0, null);
		msg.setException(new Exception("failure from ant"));
		logger.messageLogged(msg);

		err.flush();

		assertTrue(errBytes.toString()
				.startsWith("java.lang.Exception: failure from ant\n" + "	"));
	}

	@Test
	public void messageIsPrintedIffPriorityNotMoreThanSetLevel() {
		logger.setMessageOutputLevel(1);

		logger.messageLogged(event(0, "1:0"));
		logger.messageLogged(event(1, "1:1"));
		logger.messageLogged(event(2, "1 shall NOT print 2"));
		logger.messageLogged(event(3, "1 shall NOT print 3"));

		logger.setMessageOutputLevel(2);

		logger.messageLogged(event(0, "2:0"));
		logger.messageLogged(event(1, "2:1"));
		logger.messageLogged(event(2, "2:2"));
		logger.messageLogged(event(3, "2 shall NOT print 3"));

		err.flush();

		assertEquals(
				"[ant]   1:0\n" + "[ant]   1:1\n" + "[ant]   2:0\n"
						+ "[ant]   2:1\n" + "[ant]   2:2\n" + "",
				errBytes.toString());
	}

}
