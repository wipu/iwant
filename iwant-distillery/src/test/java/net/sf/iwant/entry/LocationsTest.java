package net.sf.iwant.entry;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.testarea.TestArea;

public class LocationsTest extends TestCase {

	private static void assertUrl(URL expected, URL actual) {
		assertEquals(expected.toExternalForm(), actual.toExternalForm());
	}

	public void testNetworkGetter() {
		TestArea testArea = new IwantEntryTestArea();
		IwantNetwork network = new IwantNetworkMock(testArea);
		Iwant iwant = Iwant.using(network);

		assertSame(network, iwant.network());
	}

	public void testSvnkitUrlConvenienceGetter() throws MalformedURLException {
		TestArea testArea = new IwantEntryTestArea();
		IwantNetworkMock network = new IwantNetworkMock(testArea);
		Iwant iwant = Iwant.using(network);

		network.hasSvnkitUrl("file:///something");

		assertUrl(new URL("file:///something"), iwant.svnkitUrl());
	}

	public void testRealUrls() throws MalformedURLException {
		assertUrl(
				new URL(
						"http://www.svnkit.com/org.tmatesoft.svn_1.3.5.standalone.nojna.zip"),
				Iwant.usingRealNetwork().svnkitUrl());
	}

	public void testRealCacheLocationEscapesUrl() throws MalformedURLException {
		URL nastyUrl = new URL(
				"http://localhost/very/../nasty?url&needs=\"escaping");
		assertEquals(System.getProperty("user.home")
				+ "/.net.sf.iwant/cached/UnmodifiableUrl/"
				+ "http%3A/%2Flocalhost/very%2F..%2Fnasty?url%26"
				+ "needs%3D%22escaping", Iwant.usingRealNetwork().network()
				.cacheLocation(new UnmodifiableUrl(nastyUrl)).getAbsolutePath());
	}

}
