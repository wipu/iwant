package net.sf.iwant.entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

public class UnzippedSvnkitTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	public void testSvnkitIsDownloadedAndUnzippedToCacheIfCacheDoesNotExist() {
		File svnkit = iwant.unzippedSvnkit();
		assertEquals("[dir]", Arrays.toString(svnkit.list()));
	}

	public void testCachedUnzippedSvnkitIsReturnedWithoutDownloadingIfItExists()
			throws IOException {
		File svnkit = iwant.unzippedSvnkit();
		TestArea.ensureEmpty(svnkit);
		File svnkitJar = new File(svnkit, "svnkit.jar");
		new FileWriter(svnkitJar).append("svnkit.jar content").close();

		File svnkitAgain = iwant.unzippedSvnkit();

		assertEquals("[svnkit.jar]", Arrays.toString(svnkitAgain.list()));
		assertEquals("svnkit.jar content", testArea.contentOf(svnkitJar));
	}

}
