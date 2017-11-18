package org.fluentjava.iwant.entry;

public class Iwant {

	/**
	 * This proves that the classpath finds this for the mocked entry2 in a
	 * mocked setup instead of the one that initiated the bootstrap.
	 */
	public static String helloFromMockedEntryOne() {
		return "hello from mocked entry one.";
	}

}
