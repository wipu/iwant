package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.MalformedURLException;
import java.net.URL;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.Test;

public class LocationsTest {

	@Test
	public void networkGetter() {
		TestArea testArea = TestArea.forTest(this);
		IwantNetwork network = new IwantNetworkMock(testArea);
		Iwant iwant = Iwant.using(network);

		assertSame(network, iwant.network());
	}

	@Test
	public void realCacheLocationEscapesUrl() throws MalformedURLException {
		URL nastyUrl = new URL(
				"http://localhost/very/../nasty?url&needs=\"escaping");
		assertEquals(
				System.getProperty("user.home")
						+ "/.org.fluentjava.iwant/cached/UnmodifiableUrl/"
						+ "http%3A/%2Flocalhost/very%2F..%2Fnasty?url%26"
						+ "needs%3D%22escaping",
				Iwant.usingRealNetwork().network()
						.cacheOfContentFrom(new UnmodifiableUrl(nastyUrl))
						.getAbsolutePath());
	}

}
