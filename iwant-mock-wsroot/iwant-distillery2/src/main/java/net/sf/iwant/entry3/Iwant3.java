package net.sf.iwant.entry3;

import java.util.Arrays;

/**
 * Mock
 */
public class Iwant3 {

	public static void main(String[] args) {
		System.out.println("Mocked " + Iwant3.class.getCanonicalName());
		System.out.println("args: " + Arrays.toString(args));
	}

	public static String helloFromMockedIwant3() {
		return "Hello from mocked Iwant3";
	}

}
