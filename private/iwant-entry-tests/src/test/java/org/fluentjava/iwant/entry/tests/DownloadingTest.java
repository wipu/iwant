package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DownloadingTest {

	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private void cachedFileContains(URL url, String content) {
		File cached = iwant.network()
				.cacheOfContentFrom(new UnmodifiableUrl(url));
		Iwant.textFileEnsuredToHaveContent(cached, content);
	}

	private File remoteFileContains(String path, String content) {
		File remoteFile = new File(testArea.root(), path);
		Iwant.textFileEnsuredToHaveContent(remoteFile, content);
		return remoteFile;
	}

	@Test
	public void cachedFileIsReturnedWithoutDownloadingIfItExists() {
		URL url = Iwant.fileToUrl(new File(testArea.root(),
				"non-existent-so-impossible-to-download"));
		network.cachesUrlAt(url, "url");
		cachedFileContains(url, "cached-content");

		File cached = iwant.downloaded(url);

		assertEquals("cached-content", testArea.contentOf(cached));
	}

	@Test
	public void fileIsDownloadedToCacheDoesNotExist() {
		File remoteFile = remoteFileContains("remote", "remote-content");
		URL remoteUrl = Iwant.fileToUrl(remoteFile);
		network.cachesUrlAt(remoteUrl, "cached-remote");

		File cached = iwant.downloaded(remoteUrl);

		assertEquals("remote-content", testArea.contentOf(cached));
	}

	@Test
	public void fileIsSuccessfullyDownloadedEvenIfCacheParentDirDoesNotExist() {
		File remoteFile = remoteFileContains("remote", "remote-content");
		URL remoteUrl = Iwant.fileToUrl(remoteFile);
		network.cachesUrlAt(remoteUrl, "cached/remote");

		File cached = iwant.downloaded(remoteUrl);

		assertEquals(new File(testArea.root(), "cached/remote"), cached);
		assertEquals("remote-content", testArea.contentOf(cached));
	}

}
