package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

public class DownloadingTest extends TestCase {

	private IwantEntryTestArea testArea;
	private Iwant3NetworkMock network;
	private Iwant iwant;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new Iwant3NetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private void cachedFileContains(URL url, String content) throws IOException {
		new FileWriter(iwant.toCachePath(url)).append(content).close();
	}

	private File remoteFileContains(String path, String content)
			throws IOException {
		File remoteFile = new File(testArea.root(), path);
		new FileWriter(remoteFile).append(content).close();
		return remoteFile;
	}

	public void testCachedFileIsReturnedWithoutDownloadingIfItExists()
			throws IOException {
		URL url = new File(testArea.root(),
				"non-existent-so-impossible-to-download").toURI().toURL();
		cachedFileContains(url, "cached-content");
		File cached = iwant.downloaded(url);
		assertEquals("cached-content", testArea.contentOf(cached));
	}

	public void testFileIsDownloadedToCacheIfItDoesNotExist()
			throws IOException {
		File remoteFile = remoteFileContains("remote", "remote-content");
		URL url = remoteFile.toURI().toURL();
		File cached = iwant.downloaded(url);
		assertEquals("remote-content", testArea.contentOf(cached));
	}

}
