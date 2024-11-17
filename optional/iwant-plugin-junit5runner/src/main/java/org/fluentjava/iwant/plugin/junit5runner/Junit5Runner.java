package org.fluentjava.iwant.plugin.junit5runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.platform.console.ConsoleLauncher;

public class Junit5Runner {

	public static void main(String[] args) {
		int exitCode = -1;
		try {
			exitCode = run(args);
		} finally {
			System.exit(exitCode);
		}
	}

	public static int run(String[] args) {
		List<String> runnerArgs = new ArrayList<>();

		for (String arg : args) {
			runnerArgs.add("-c");
			runnerArgs.add(arg);
		}
		if (runnerArgs.isEmpty()) {
			System.err.println("No tests given, exiting.");
			return 0;
		}

		try (PrintWriter out = new PrintWriter(System.out);
				PrintWriter err = new PrintWriter(System.err)) {
			return ConsoleLauncher
					.run(out, err, runnerArgs.toArray(new String[0]))
					.getExitCode();
		}
	}

}
