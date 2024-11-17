package org.fluentjava.iwant.plugin.testng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.ExitCalledException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestngRunnerTest {

	private static SecurityManager originalSecurityManager;

	@BeforeAll
	public static void beforeClass() {
		originalSecurityManager = System.getSecurityManager();
		System.setSecurityManager(new Iwant.ExitCatcher());
	}

	@AfterAll
	public static void afterClass() {
		System.setSecurityManager(originalSecurityManager);
	}

	@BeforeEach
	public void before() {
		Successful1.test1Executed = false;
		Successful1.test2Executed = false;
		Successful2.test1Executed = false;
		Successful2.test2Executed = false;
		Failing.test1Executed = false;
		Failing.test2Executed = false;
	}

	@Test
	public void mainClassIsSelf() {
		assertEquals("org.fluentjava.iwant.plugin.testng.TestngRunner",
				new TestngRunner().mainClassName());
	}

	@Test
	public void runOfSuccessfulTests() {
		try {
			TestngRunner
					.main(new String[] { Successful1.class.getCanonicalName(),
							Successful2.class.getCanonicalName() });
			fail();
		} catch (ExitCalledException e) {
			assertEquals(0, e.status());
		}

		assertTrue(Successful1.test1Executed);
		assertTrue(Successful1.test2Executed);
		assertTrue(Successful2.test1Executed);
		assertTrue(Successful2.test2Executed);
	}

	@Test
	public void testFailure() {
		try {
			TestngRunner.main(new String[] { Failing.class.getCanonicalName(),
					Successful2.class.getCanonicalName() });
		} catch (ExitCalledException e) {
			assertEquals(1, e.status());
		}

		assertTrue(Failing.test1Executed);
		assertTrue(Failing.test2Executed);
		assertTrue(Successful2.test1Executed);
		assertTrue(Successful2.test2Executed);
	}

}
