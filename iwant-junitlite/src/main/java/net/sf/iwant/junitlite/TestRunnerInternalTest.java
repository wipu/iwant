package net.sf.iwant.junitlite;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestRunnerInternalTest extends TestCase {

	static void test() {
		TestRunnerInternalTest test = new TestRunnerInternalTest();
		System.err.println("Testing TestRunner");
		test.testOneFailingMethod();
		test.testOnePassingMethod();
		test.testManyMethodTest();
		test.testSetupAndTeardown();
		test.testClassWithSuite();
		System.err.println("TestRunner OK");
	}

	public static class OneMethodTest {

		private static boolean mustFail = false;

		public void testConditionalPass() {
			if (mustFail)
				throw new IllegalStateException("I fail!");
		}

	}

	public void testOneFailingMethod() {
		OneMethodTest.mustFail = true;
		try {
			TestRunner.run(OneMethodTest.class);
			fail();
		} catch (Throwable e) {
			// expected
		}
	}

	public void testOnePassingMethod() {
		OneMethodTest.mustFail = false;
		TestRunner.run(OneMethodTest.class);
	}

	public static class ManyMethodTest {

		private static List<String> methodsCalled = new ArrayList();

		public void nonTest() {
			throw new IllegalArgumentException("Shouldn't be called");
		}

		public void testValid1() {
			methodsCalled.add("testValid1");
		}

		public void testInvalid(String s) {
			throw new IllegalArgumentException("Shouldn't be called");
		}

		public String testInvalid() {
			throw new IllegalArgumentException("Shouldn't be called");
		}

		public void testValid2() {
			methodsCalled.add("testValid2");
		}

	}

	public void testManyMethodTest() {
		ManyMethodTest.methodsCalled.clear();
		TestRunner.run(ManyMethodTest.class);
		assertEquals("[testValid1, testValid2]", ManyMethodTest.methodsCalled
				.toString());
	}

	public static class TestWithSetupAndTeardown {

		private static final List<String> methodsCalled = new ArrayList();

		public void setUp() {
			methodsCalled.add("setUp");
		}

		public void tearDown() {
			methodsCalled.add("tearDown");
		}

		public void testPass() {
			methodsCalled.add("testPass");
		}

		public void testFailure() {
			methodsCalled.add("testFailure");
			throw new IllegalArgumentException("I fail!");
		}

	}

	public void testSetupAndTeardown() {
		TestWithSetupAndTeardown.methodsCalled.clear();
		try {
			TestRunner.run(TestWithSetupAndTeardown.class);
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		assertEquals(
				"[setUp, testPass, tearDown, setUp, testFailure, tearDown]",
				TestWithSetupAndTeardown.methodsCalled.toString());
	}

	// TODO test failing setUp and tearDown

	public static class ClassWithSuite {

		public static Test suite() {
			TestSuite suite = new TestSuite("a suite");
			suite.addTestSuite(ManyMethodTest.class);
			suite.addTestSuite(ManyMethodTest.class);
			return suite;
		}

	}

	public void testClassWithSuite() {
		ManyMethodTest.methodsCalled.clear();
		TestRunner.run(ClassWithSuite.class);
		assertEquals("[testValid1, testValid2, testValid1, testValid2]",
				ManyMethodTest.methodsCalled.toString());
	}

}
