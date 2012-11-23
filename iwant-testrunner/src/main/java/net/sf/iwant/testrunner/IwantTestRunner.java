package net.sf.iwant.testrunner;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.TestRunner;

public class IwantTestRunner {

	/**
	 * TODO why doesn't TestRunner find the class when run from outside jvm, and
	 * not even when calling run(Class) from here.
	 */
	public static void main(String[] args) throws Exception {
		if (!"/".equals(System.getProperty("file.separator"))) {
			System.err
					.println("Sorry, self-test not supported on your operating system.");
			return;
		}
		String suiteName = args[0];
		Class<?> suiteClass = Class.forName(suiteName);
		Method suiteMethod = suiteClass.getMethod("suite");
		Test suite = (Test) suiteMethod.invoke(null);
		TestResult result = TestRunner.run(suite);
		if (!result.wasSuccessful()) {
			throw new IllegalStateException("Test failed.");
		}
	}

}
