package org.fluentjava.iwant.plugin.testng;

import java.util.ArrayList;
import java.util.List;

import org.fluentjava.iwant.api.javamodules.TestRunner;
import org.testng.TestNG;

public class TestngRunner implements TestRunner {

	public static final TestngRunner INSTANCE = new TestngRunner();

	public static void main(String[] args) {
		List<String> newArgs = new ArrayList<>();
		newArgs.add("-testclass");
		newArgs.add(commaSeparated(args));
		TestNG.main(newArgs.toArray(new String[0]));
	}

	@Override
	public String mainClassName() {
		return getClass().getCanonicalName();
	}

	private static String commaSeparated(String[] args) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				b.append(",");
			}
			b.append(args[i]);
		}
		return b.toString();
	}

}
