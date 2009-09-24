package net.sf.iwant.junitlite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestSuite;

public class TestRunner {

	public static void main(String[] args) throws Exception {
		TestRunnerInternalTest.test();
		for (String arg : args) {
			Class testClass = Class.forName(arg);
			run(testClass);
		}
	}

	public static void run(Class testClass) {
		System.err.print(testClass.getSimpleName());
		Object test = instantiate(testClass);
		Method setUp = getSetUpMethod(testClass);
		Method tearDown = getTearDownMethod(testClass);
		for (Method m : testClass.getMethods()) {
			if (isTestMethod(m)) {
				if (setUp != null)
					invoke(test, setUp);
				try {
					invoke(test, m);
				} finally {
					if (tearDown != null)
						invoke(test, tearDown);
				}
				System.err.print(".");
			}
		}
		System.err.println();
		Method suiteMethod = getSuiteMethod(testClass);
		if (suiteMethod != null) {
			TestSuite suite = invokeSuiteMethod(suiteMethod);
			System.err.println("(" + suite.getName() + ")");
			for (Class subTest : suite.getSubTests()) {
				run(subTest);
			}
		}
	}

	private static TestSuite invokeSuiteMethod(Method suiteMethod) {
		try {
			TestSuite suite = (TestSuite) suiteMethod.invoke(null);
			return suite;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Method getSuiteMethod(Class testClass) {
		return getMethod(testClass, "suite");
	}

	private static Method getSetUpMethod(Class testClass) {
		return getMethod(testClass, "setUp");
	}

	private static Method getTearDownMethod(Class testClass) {
		return getMethod(testClass, "tearDown");
	}

	private static Method getMethod(Class testClass, String name) {
		try {
			return testClass.getMethod(name);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static void invoke(Object test, Method m) {
		try {
			m.invoke(test);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Object instantiate(Class testClass) {
		try {
			return testClass.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static boolean isTestMethod(Method m) {
		if (!m.getName().startsWith("test"))
			return false;
		if (!m.getGenericReturnType().equals(Void.TYPE))
			return false;
		if (m.getParameterTypes().length > 0)
			return false;
		return true;
	}

}
