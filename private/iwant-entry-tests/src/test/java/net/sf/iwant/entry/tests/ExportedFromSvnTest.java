package net.sf.iwant.entry.tests;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.testarea.TestArea;

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

	public void testUrlAppendForLearning() throws MalformedURLException {
		URL base = new URL("http://localhost/base");
		// it replaces the path:
		assertEquals("http://localhost/sub",
				new URL(base, "sub").toExternalForm());
		// so we need string append here:
		assertEquals("http://localhost/base/sub",
				new URL(base + "/sub").toExternalForm());
	}

	public void testSubUrlOfFileSvnUrlWithoutRevision()
			throws MalformedURLException {
		assertEquals(
				"file:/revless/url/optional/iwant-plugin-ant",
				Iwant.subUrlOfSvnUrl(new URL("file:///revless/url"),
						"optional/iwant-plugin-ant").toExternalForm());
	}

	public void testSubUrlOfFileSvnUrlWithRevision()
			throws MalformedURLException {
		assertEquals(
				"file:/local/url@123/optional/iwant-plugin-ant",
				Iwant.subUrlOfSvnUrl(new URL("file:///local/url@123"),
						"optional/iwant-plugin-ant").toExternalForm());
	}

	public void testSubUrlOfHttpsSvnUrlWithRevision()
			throws MalformedURLException {
		assertEquals(
				"https://svn.code.sf.net/p/iwant/code/trunk/essential@687",
				Iwant.subUrlOfSvnUrl(
						new URL(
								"https://svn.code.sf.net/p/iwant/code/trunk@687"),
						"essential").toExternalForm());
	}

	public void testExportReturnsDifferentFileFromSourceWithCorrectContent() {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.cachesUrlAt(remoteUrl, "svn-exported");

		File exported = iwant.exportedFromSvn(remoteUrl, true);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "essential/iwant-entry2/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"essential/iwant-entry/as-some-developer/with/java/"
						+ "net/sf/iwant/entry/Iwant.java").exists());
	}

	public void testExportIsDoneFromFileEvenWithoutReExportPermissionWhenLocalDoesNotExist() {
		File remote = IwantWsRootFinder.mockWsRoot();
		URL remoteUrl = Iwant.fileToUrl(remote);
		network.usesRealSvnkitUrlAndCacheAndUnzipped();
		network.cachesUrlAt(remoteUrl, "svn-exported");

		File exported = iwant.exportedFromSvn(remoteUrl, false);

		assertFalse(exported.equals(remote));
		assertTrue(new File(exported, "essential/iwant-entry2/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
		assertTrue(new File(exported,
				"essential/iwant-entry/as-some-developer/with/java/"
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
		assertTrue(new File(exportedAgain,
				"essential/iwant-entry2/src/main/java/"
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
		assertFalse(new File(exportedAgain, "iwant-entry2/src/main/java/"
				+ "net/sf/iwant/entry2/Iwant2.java").exists());
	}

}
