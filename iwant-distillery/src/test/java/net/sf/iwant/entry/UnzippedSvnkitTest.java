package net.sf.iwant.entry;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.iwant.testing.IwantEntryTestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class UnzippedSvnkitTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@Override
	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private URL dirContainingAAndBZip() {
		return getClass().getResource("dir-containing-a-and-b.zip");
	}

	public void testSvnkitIsDownloadedAndUnzippedToCacheIfCacheDoesNotExist() {
		network.hasSvnkitUrl(dirContainingAAndBZip());
		File downloadedSvnkit = network.cachesUrlAt(dirContainingAAndBZip(),
				"downloaded-svnkit");
		network.cachesZipAt(Iwant.fileToUrl(downloadedSvnkit),
				"unzipped-svnkit");

		File svnkit = iwant.unzippedSvnkit();

		assertEquals("[dir]", Arrays.toString(svnkit.list()));
	}

	public void testCachedUnzippedSvnkitIsReturnedWithoutDownloadingIfItExists() {
		network.hasSvnkitUrl(dirContainingAAndBZip());
		network.cachesUrlAt(dirContainingAAndBZip(), "downloaded-svnkit");
		File downloadedSvnkit = testArea.newDir("downloaded-svnkit");
		File unzippedSvnkit = testArea.newDir("unzipped-svnkit");
		File svnkitJar = new File(unzippedSvnkit, "svnkit.jar");
		Iwant.newTextFile(svnkitJar, "svnkit.jar content");
		network.cachesZipAt(Iwant.fileToUrl(downloadedSvnkit),
				"unzipped-svnkit");

		File unzippedSvnkitAgain = iwant.unzippedSvnkit();

		assertEquals("[svnkit.jar]",
				Arrays.toString(unzippedSvnkitAgain.list()));
		assertEquals("svnkit.jar content", testArea.contentOf(svnkitJar));
	}

}
