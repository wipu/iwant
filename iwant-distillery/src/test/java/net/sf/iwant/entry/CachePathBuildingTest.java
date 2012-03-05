package net.sf.iwant.entry;

import junit.framework.TestCase;

public class CachePathBuildingTest extends TestCase {

	private static final String HOME = System.getProperty("user.home");

	private static void urlToCachePath(String url, String cachePath) {
		assertEquals(cachePath, Iwant.usingRealNetwork().toCachePath(url));
	}

	public void testUrlsWithCharactersNotAllowedInFileNames() {
		urlToCachePath("file:///some/file", HOME
				+ "/.iwant/wanted/by-url/file%3A%2F%2F%2Fsome%2Ffile");
		urlToCachePath("http://some.host:8080/o'clock?a=1&b=2", HOME
				+ "/.iwant/wanted/by-url/http%3A%2F%2Fsome.host%3A8080%2F"
				+ "o%27clock%3Fa%3D1%26b%3D2");
	}

}
