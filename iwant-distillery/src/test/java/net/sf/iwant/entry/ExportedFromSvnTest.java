package net.sf.iwant.entry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class ExportedFromSvnTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMockWithRealSvnkitLocations network;
	private Iwant iwant;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMockWithRealSvnkitLocations(testArea);
		iwant = Iwant.using(network);
	}

	private static class IwantNetworkMockWithRealSvnkitLocations extends
			IwantNetworkMock {

		public IwantNetworkMockWithRealSvnkitLocations(
				IwantEntryTestArea testArea) {
			super(testArea);
		}

		@Override
		public File wantedUnmodifiable(URL url) {
			if (svnkitUrl().equals(url)) {
				return Iwant.usingRealNetwork().network()
						.wantedUnmodifiable(url);
			} else {
				return super.wantedUnmodifiable(url);
			}
		}

		@Override
		public URL svnkitUrl() {
			// here we assume download and unzip have been tested
			return Iwant.usingRealNetwork().svnkitUrl();
		}

	}

	public void testExportReturnsDifferentFileFromSourceWithCorrectContent()
			throws MalformedURLException {
		File remote = WsRootFinder.mockWsRoot();
		URL remoteUrl = remote.toURI().toURL();

		File exported = iwant.exportedFromSvn(remoteUrl);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"iwant-distillery/as-some-developer/with/java/"
						+ "net/sf/iwant/entry/Iwant.java").exists());
	}

}
