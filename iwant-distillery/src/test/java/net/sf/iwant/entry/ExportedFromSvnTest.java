package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;

public class ExportedFromSvnTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@Override
	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		iwant = Iwant.using(network);
	}

	public void testExportReturnsDifferentFileFromSourceWithCorrectContent() {
		File remote = WsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.cachesUrlAt(remoteUrl, "svn-exported");

		File exported = iwant.exportedFromSvn(remoteUrl);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"iwant-distillery/as-some-developer/with/java/"
						+ "net/sf/iwant/entry/Iwant.java").exists());
	}

	public void testNothingIsExportedIfLocalFileExists() throws IOException {
		// an url that is assumed to fail:
		URL remoteUrl = new URL("http://nonexistent/not-to-be-accessed-by-"
				+ getClass());
		File exportedDir = testArea.newDir("exported");
		File exportedFile = new File(exportedDir, "exported-file");
		new FileWriter(exportedFile).append("exported-content").close();
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exportedDir);

		File exportedAgain = iwant.exportedFromSvn(remoteUrl);

		assertEquals(exportedDir.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertEquals("exported-content", testArea.contentOf(exportedFile));
	}

	/**
	 * Local may have changed. For example local iwant needs to be re-exported
	 * each time for change to be effective.
	 */
	public void testExportIsRedoneIfUrlSchemeIsFile() throws IOException {
		File remote = WsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		File exported = testArea.newDir("exported");
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exported);
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
