package org.fluentjava.iwant.entry2;

import org.fluentjava.iwant.entry.Iwant;

public class Iwant2 {

	public static void main(String[] args) {
		System.out.println("Mocked iwant entry2");
		System.out.println("CWD: " + System.getProperty("user.dir"));
		System.out.println("args: " + java.util.Arrays.toString(args));
		System.err.println("And syserr message from mocked entry2");
		System.out.println("And " + Iwant.helloFromMockedEntryOne());
	}

}
