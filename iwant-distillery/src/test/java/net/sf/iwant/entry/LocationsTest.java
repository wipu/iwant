package net.sf.iwant.entry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;

public class LocationsTest extends TestCase {

	private static void assertUrl(URL expected, URL actual) {
		assertEquals(expected.toExternalForm(), actual.toExternalForm());
	}

	public void testMockedLocations() {
		TestArea testArea = new IwantEntryTestArea();
		IwantNetwork network = new Iwant3NetworkMock(testArea);
		Iwant iwant = Iwant.using(network);

		assertUrl(network.svnkitUrl(), iwant.svnkitUrl());
		assertEquals(network.wantedUnmodifiable(null), iwant.network()
				.wantedUnmodifiable(null));
	}

	public void testRealLocations() throws MalformedURLException {
		assertUrl(
				new URL(
						"http://www.svnkit.com/org.tmatesoft.svn_1.3.5.standalone.nojna.zip"),
				Iwant.usingRealNetwork().svnkitUrl());
		assertEquals(new File(System.getProperty("user.home")
				+ "/.net.sf.iwant/wanted-unmodifiable"), Iwant
				.usingRealNetwork().network().wantedUnmodifiable(null));
	}

}
