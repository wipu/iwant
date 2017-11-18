package org.fluentjava.iwant.entry3;

import java.net.URLClassLoader;
import java.util.Arrays;

import org.fluentjava.iwant.api.wsdef.IwantWorkspace;

/**
 * Mock
 */
public class Iwant3 {

	public static void main(String[] args) {
		System.out.println("Mocked " + Iwant3.class.getCanonicalName());
		System.out.println("args: " + Arrays.toString(args));
		if (Arrays.toString(args).contains("--printClassLoaderUrls")) {
			URLClassLoader cl = (URLClassLoader) Iwant3.class.getClassLoader();
			System.out.println(
					"classloader urls: " + Arrays.toString(cl.getURLs()));
		}
	}

	public static String helloFromMockedIwant3() {
		return "Hello from mocked Iwant3, IwantWorkspace: "
				+ IwantWorkspace.class.getCanonicalName();
	}

}
