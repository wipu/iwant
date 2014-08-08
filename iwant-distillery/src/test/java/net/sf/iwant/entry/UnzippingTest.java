package net.sf.iwant.entry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant.UnmodifiableZip;
import net.sf.iwant.testing.IwantEntryTestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class UnzippingTest extends TestCase {

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

	public void testStreamIsCorrectlyUnzippedToCacheWhenCacheDoesNotExist()
			throws FileNotFoundException, IOException {
		URL url = dirContainingAAndBZip();
		UnmodifiableZip src = new UnmodifiableZip(url);
		network.cachesAt(src, "unzipped");

		File unzipped = iwant.unmodifiableZipUnzipped(src);

		assertEquals(testArea.root() + "/unzipped", unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[dir]", Arrays.toString(unzipped.list()));
		assertEquals("a\n", testArea.contentOf(new File(unzipped, "dir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "dir/b")));
	}

	public void testCacheIsReturnedWithoutUnzippingWhenCacheExists() {
		URL zip = Iwant.fileToUrl(new File(testArea.root(),
				"not-to-be-accessed"));
		File unzipped = network.cachesZipAt(zip, "unzipped");
		Iwant.newTextFile(new File(unzipped, "file"), "unzipped content");

		File unzippedAgain = iwant.unmodifiableZipUnzipped(new UnmodifiableZip(
				zip));

		assertEquals(unzipped, unzippedAgain);
		assertTrue(unzipped.isDirectory());
		assertEquals("unzipped content", testArea.contentOf("unzipped/file"));
	}

	public void testUnzipHandlesSubDirectoriesToo()
			throws FileNotFoundException, IOException {
		URL url = getClass().getResource("zip-with-subdir.zip");
		UnmodifiableZip src = new UnmodifiableZip(url);
		network.cachesAt(src, "unzipped");

		File unzipped = iwant.unmodifiableZipUnzipped(src);

		assertEquals(testArea.root() + "/unzipped", unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[root]", Arrays.toString(unzipped.list()));
		assertEquals("a\n",
				testArea.contentOf(new File(unzipped, "root/subdir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "root/b")));
	}

	public void testNeededJarsAreUnzippedFromRealSvnkitZip()
			throws FileNotFoundException, IOException {
		URL realCachedSvnkit = Iwant.fileToUrl(Iwant.usingRealNetwork()
				.downloaded(Iwant.usingRealNetwork().svnkitUrl()));
		UnmodifiableZip src = new UnmodifiableZip(realCachedSvnkit);
		network.cachesAt(src, "unzipped");

		File unzipped = iwant.unmodifiableZipUnzipped(src);

		assertEquals(testArea.root() + "/unzipped", unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[svnkit-1.8.6]", Arrays.toString(unzipped.list()));
		assertEquals(4035241, new File(unzipped,
				"svnkit-1.8.6/lib/svnkit-1.8.6.jar").length());
		assertEquals(365477, new File(unzipped,
				"svnkit-1.8.6/lib/svnkit-cli-1.8.6.jar").length());
	}

}
