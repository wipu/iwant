package org.fluentjava.iwant.plugin.jacoco;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.entry.Iwant;
import org.junit.platform.console.ConsoleLauncher;

public class Junit5Runner {

	public static void main(String[] args) {
		Iwant.debugLog(Junit5Runner.class.getCanonicalName(),
				Arrays.toString(args));
		List<String> runnerArgs = new ArrayList<>();

		for (String arg : args) {
			runnerArgs.add("-c");
			runnerArgs.add(arg);
		}
		if (runnerArgs.isEmpty()) {
			Iwant.debugLog(Junit5Runner.class.getCanonicalName(),
					"No tests given, exiting.");
			return;
		}

		int exitCode = 1;
		try (PrintWriter out = new PrintWriter(System.out);
				PrintWriter err = new PrintWriter(System.err)) {
			exitCode = ConsoleLauncher
					.run(out, err, runnerArgs.toArray(new String[0]))
					.getExitCode();
		} finally {
			System.exit(exitCode);
		}
	}
}
