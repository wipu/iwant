package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;

public class DownloadingTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@Override
	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private void cachedFileContains(URL url, String content) throws IOException {
		File cached = iwant.network().cacheLocation(new UnmodifiableUrl(url));
		new FileWriter(cached).append(content).close();
	}

	private File remoteFileContains(String path, String content)
			throws IOException {
		File remoteFile = new File(testArea.root(), path);
		new FileWriter(remoteFile).append(content).close();
		return remoteFile;
	}

	public void testCachedFileIsReturnedWithoutDownloadingIfItExists()
			throws IOException {
		URL url = Iwant.fileToUrl(new File(testArea.root(),
				"non-existent-so-impossible-to-download"));
		network.cachesUrlAt(url, "url");
		cachedFileContains(url, "cached-content");

		File cached = iwant.downloaded(url);

		assertEquals("cached-content", testArea.contentOf(cached));
	}

	public void testFileIsDownloadedToCacheDoesNotExist() throws IOException {
		File remoteFile = remoteFileContains("remote", "remote-content");
		URL remoteUrl = Iwant.fileToUrl(remoteFile);
		network.cachesUrlAt(remoteUrl, "cached-remote");

		File cached = iwant.downloaded(remoteUrl);

		assertEquals("remote-content", testArea.contentOf(cached));
	}

	public void testFileIsSuccessfullyDownloadedEvenIfCacheParentDirDoesNotExist()
			throws IOException {
		File remoteFile = remoteFileContains("remote", "remote-content");
		URL remoteUrl = Iwant.fileToUrl(remoteFile);
		network.cachesUrlAt(remoteUrl, "cached/remote");

		File cached = iwant.downloaded(remoteUrl);

		assertEquals(new File(testArea.root(), "cached/remote"), cached);
		assertEquals("remote-content", testArea.contentOf(cached));
	}

}
