package net.sf.iwant.entry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import junit.framework.TestCase;

public class UnzippingTest extends TestCase {

	private IwantEntryTestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	public void setUp() {
		testArea = new IwantEntryTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	public void testStreamIsCorrectlyUnzippedToCacheWhenCacheDoesNotExist()
			throws FileNotFoundException, IOException {
		InputStream in = network.svnkitUrl().openStream();
		File unzipped = iwant.unmodifiableZipUnzipped(new URL(
				"http:nasty/name&"), in);
		assertEquals(network.wantedUnmodifiable(null)
				+ "/unzipped/http%3Anasty%2Fname%26",
				unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[dir]", Arrays.toString(unzipped.list()));
		assertEquals("a\n", testArea.contentOf(new File(unzipped, "dir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "dir/b")));
	}

	public void testCacheIsReturnedWithoutUnzippingWhenCacheExists()
			throws FileNotFoundException, IOException {
		testStreamIsCorrectlyUnzippedToCacheWhenCacheDoesNotExist();
		// null in is supposed to prove it's not used
		File unzipped = iwant.unmodifiableZipUnzipped(new URL(
				"http:nasty/name&"), null);
		assertEquals(network.wantedUnmodifiable(null)
				+ "/unzipped/http%3Anasty%2Fname%26",
				unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

	}

	public void testUnzipHandlesSubDirectoriesToo()
			throws FileNotFoundException, IOException {
		InputStream in = getClass().getResource("zip-with-subdir.zip")
				.openStream();
		File unzipped = iwant.unmodifiableZipUnzipped(new URL(
				"http:nasty/name&"), in);
		assertEquals(network.wantedUnmodifiable(null)
				+ "/unzipped/http%3Anasty%2Fname%26",
				unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[root]", Arrays.toString(unzipped.list()));
		assertEquals("a\n",
				testArea.contentOf(new File(unzipped, "root/subdir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "root/b")));
	}

}
