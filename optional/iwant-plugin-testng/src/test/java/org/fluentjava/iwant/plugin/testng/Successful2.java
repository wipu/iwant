package org.fluentjava.iwant.plugin.testng;

import org.testng.annotations.Test;

public class Successful2 {

	static boolean test1Executed;
	static boolean test2Executed;

	@Test
	public void test1() {
		test1Executed = true;
	}

	@Test
	public void test2() {
		test2Executed = true;
	}

}
