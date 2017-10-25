package net.sf.iwant.entry.tests;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.testarea.TestArea;

public class LocationsTest extends TestCase {

	public void testNetworkGetter() {
		TestArea testArea = TestArea.forTest(this);
		IwantNetwork network = new IwantNetworkMock(testArea);
		Iwant iwant = Iwant.using(network);

		assertSame(network, iwant.network());
	}

	public void testRealCacheLocationEscapesUrl() throws MalformedURLException {
		URL nastyUrl = new URL(
				"http://localhost/very/../nasty?url&needs=\"escaping");
		assertEquals(
				System.getProperty("user.home")
						+ "/.net.sf.iwant/cached/UnmodifiableUrl/"
						+ "http%3A/%2Flocalhost/very%2F..%2Fnasty?url%26"
						+ "needs%3D%22escaping",
				Iwant.usingRealNetwork().network()
						.cacheOfContentFrom(new UnmodifiableUrl(nastyUrl))
						.getAbsolutePath());
	}

}
