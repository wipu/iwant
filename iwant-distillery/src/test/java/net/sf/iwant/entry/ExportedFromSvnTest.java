package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	public void testNothingIsExportedIfLocalFileExists() throws IOException {
		// an url that most probably fails if accessed:
		URL remote = new URL("http://localhost/"
				+ getClass().getCanonicalName() + "/assumedly-already-exported");
		File exported = iwant.toCachePath(remote);
		Iwant.ensureDir(exported);
		File exportedFile = new File(exported, "file");
		new FileWriter(exportedFile).append("exported-content").close();

		File exportedAgain = iwant.exportedFromSvn(remote);

		assertEquals(exported.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertEquals("exported-content", testArea.contentOf(exportedFile));
	}

	/**
	 * Local may have changed. For example local iwant needs to be re-exported
	 * each time for change to be effective.
	 */
	public void testExportIsRedoneIfUrlSchemeIsFile() throws IOException {
		File remote = WsRootFinder.mockWsRoot();
		URL remoteUrl = remote.toURI().toURL();

		File exported = iwant.toCachePath(remoteUrl);
		Iwant.ensureDir(exported);
		File previouslyExportedFile = new File(exported, "exported-file");
		new FileWriter(previouslyExportedFile).append("exported-content")
				.close();

		File exportedAgain = iwant.exportedFromSvn(remoteUrl);

		assertEquals(exported.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertFalse(previouslyExportedFile.exists());
		assertTrue(new File(exportedAgain, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
	}

}
