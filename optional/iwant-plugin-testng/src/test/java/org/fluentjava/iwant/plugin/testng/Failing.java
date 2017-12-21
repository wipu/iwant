package org.fluentjava.iwant.plugin.testng;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class Failing {

	static boolean test1Executed;
	static boolean test2Executed;

	@Test
	public void test1() {
		test1Executed = true;
		assertEquals(1, 2);
	}

	@Test
	public void test2() {
		test2Executed = true;
		assertEquals("a", "b");
	}

}
