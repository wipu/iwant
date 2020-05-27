package org.fluentjava.iwant.api.antrunner;

import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;

public class MinimalAntLogger implements BuildLogger {

	private PrintStream err;

	@Override
	public void setErrorPrintStream(PrintStream err) {
		this.err = new PrintStream(err, true);
	}

	private void log(Object... args) {
		err.print("[ant] ");
		for (Object arg : args) {
			err.print(arg);
		}
		err.println();
	}

	@Override
	public void buildStarted(BuildEvent event) {
		log("Starting build ", event.getProject().getName());
	}

	@Override
	public void buildFinished(BuildEvent event) {
		if (reportPossibleFailure(event)) {
			log("Build FAILED.");
		} else {
			log("Build SUCCESFUL.");
		}
	}

	private boolean reportPossibleFailure(BuildEvent event) {
		Throwable failure = event.getException();
		if (failure == null) {
			return false;
		}
		failure.printStackTrace(err);
		return true;
	}

	@Override
	public void targetStarted(BuildEvent event) {
		log("Target ", event.getTarget());
	}

	@Override
	public void targetFinished(BuildEvent event) {
		// not interesting
	}

	@Override
	public void taskStarted(BuildEvent event) {
		// not interesting
	}

	@Override
	public void taskFinished(BuildEvent event) {
		// not interesting
	}

	@Override
	public void messageLogged(BuildEvent event) {
		String msg = event.getMessage();
		if (hasInterestingMessage(msg)) {
			log("  ", event.getMessage());
		}
		reportPossibleFailure(event);
	}

	private static boolean hasInterestingMessage(String msg) {
		return msg != null && !msg.trim().isEmpty();
	}

	@Override
	public void setMessageOutputLevel(int level) {
		// not interesting
	}

	@Override
	public void setOutputPrintStream(PrintStream output) {
		// not interesting
	}

	@Override
	public void setEmacsMode(boolean emacsMode) {
		// not interesting
	}

}
