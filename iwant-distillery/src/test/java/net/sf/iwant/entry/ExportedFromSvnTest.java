package net.sf.iwant.entry;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class ExportedFromSvnTest extends TestCase {

	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		iwant = Iwant.using(network);
	}

	public void testExportReturnsDifferentFileFromSourceWithCorrectContent() {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.cachesUrlAt(remoteUrl, "svn-exported");

		File exported = iwant.exportedFromSvn(remoteUrl, true);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"iwant-distillery/as-some-developer/with/java/"
						+ "net/sf/iwant/entry/Iwant.java").exists());
	}

	public void testExportIsDoneFromFileEvenWithoutReExportPermissionWhenLocalDoesNotExist() {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.cachesUrlAt(remoteUrl, "svn-exported");

		File exported = iwant.exportedFromSvn(remoteUrl, false);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"iwant-distillery/as-some-developer/with/java/"
						+ "net/sf/iwant/entry/Iwant.java").exists());
	}

	public void testNothingIsExportedIfLocalFileExists() throws IOException {
		// an url that is assumed to fail:
		URL remoteUrl = new URL(
				"http://localhost/nonexistent/not-to-be-accessed-by-"
						+ getClass());
		File exportedDir = testArea.newDir("exported");
		File exportedFile = new File(exportedDir, "exported-file");
		Iwant.newTextFile(exportedFile, "exported-content");
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exportedDir);

		File exportedAgain = iwant.exportedFromSvn(remoteUrl, true);

		assertEquals(exportedDir.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertEquals("exported-content", testArea.contentOf(exportedFile));
	}

	public void testNothingIsExportedIfLocalFileExistsEvenWithReExportDisabled()
			throws IOException {
		// an url that is assumed to fail:
		URL remoteUrl = new URL(
				"http://localhost/nonexistent/not-to-be-accessed-by-"
						+ getClass());
		File exportedDir = testArea.newDir("exported");
		File exportedFile = new File(exportedDir, "exported-file");
		Iwant.newTextFile(exportedFile, "exported-content");
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exportedDir);

		File exportedAgain = iwant.exportedFromSvn(remoteUrl, false);

		assertEquals(exportedDir.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertEquals("exported-content", testArea.contentOf(exportedFile));
	}

	/**
	 * Local may have changed. For example local iwant needs to be re-exported
	 * each time for change to be effective.
	 */
	public void testExportIsRedoneIfUrlSchemeIsFile() throws IOException {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		File exported = testArea.newDir("exported");
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exported);
		File previouslyExportedFile = new File(exported, "exported-file");
		Iwant.newTextFile(previouslyExportedFile, "exported-content");

		File exportedAgain = iwant.exportedFromSvn(remoteUrl, true);

		assertEquals(exported.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertFalse(previouslyExportedFile.exists());
		assertTrue(new File(exportedAgain, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
	}

	/**
	 * Optimization for cases where the user knows what he's doing, like in
	 * iwant descript
	 */
	public void testExportIsNotRedoneEvenIfUrlSchemeIsFileWhenReExportDisabled()
			throws IOException {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		File exported = testArea.newDir("exported");
		network.cachesAt(new UnmodifiableUrl(remoteUrl), exported);
		File previouslyExportedFile = new File(exported, "exported-file");
		Iwant.newTextFile(previouslyExportedFile, "exported-content");

		File exportedAgain = iwant.exportedFromSvn(remoteUrl, false);

		assertEquals(exported.getCanonicalPath(),
				exportedAgain.getCanonicalPath());

		assertTrue(previouslyExportedFile.exists());
		assertFalse(new File(exportedAgain, "iwant-distillery/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
	}

}
