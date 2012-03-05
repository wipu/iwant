package net.sf.iwant.entry;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

public class CachePathBuildingTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private void urlToCachePath(String url, String cachePath)
			throws IOException {
		assertEquals(cachePath, iwant.toCachePath(new URL(url))
				.getCanonicalPath());
	}

	public void testUrlsWithCharactersNotAllowedInFileNames()
			throws IOException {
		urlToCachePath("file:///some/file", network.wantedUnmodifiable()
				+ "/file%3A%2Fsome%2Ffile");
		urlToCachePath("http://some.host:8080/o'clock?a=1&b=2",
				network.wantedUnmodifiable()
						+ "/http%3A%2F%2Fsome.host%3A8080%2F"
						+ "o%27clock%3Fa%3D1%26b%3D2");
	}

}
