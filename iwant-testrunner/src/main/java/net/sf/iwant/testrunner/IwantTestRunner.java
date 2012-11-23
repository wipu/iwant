package net.sf.iwant.testrunner;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.TestRunner;

public class IwantTestRunner {

	private static final String FILE_SEPARATOR_KEY = "file.separator";

	/**
	 * TODO why doesn't TestRunner find the class when run from outside jvm, and
	 * not even when calling run(Class) from here.
	 */
	public static void main(String[] args) throws Exception {
		// use same output even in wintoys so tests don't need changes:
		String oldFileSeparator = System.getProperty(FILE_SEPARATOR_KEY);
		System.setProperty(FILE_SEPARATOR_KEY, "/");
		try {
			String suiteName = args[0];
			Class<?> suiteClass = Class.forName(suiteName);
			Method suiteMethod = suiteClass.getMethod("suite");
			Test suite = (Test) suiteMethod.invoke(null);
			TestResult result = TestRunner.run(suite);
			if (!result.wasSuccessful()) {
				throw new IllegalStateException("Test failed.");
			}
		} finally {
			System.setProperty(FILE_SEPARATOR_KEY, oldFileSeparator);
		}
	}

}
