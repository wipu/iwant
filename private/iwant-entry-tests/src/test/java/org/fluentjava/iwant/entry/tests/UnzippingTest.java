package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableZip;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnzippingTest {

	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant iwant;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
	}

	private URL dirContainingAAndBZip() {
		return getClass().getResource("dir-containing-a-and-b.zip");
	}

	@Test
	public void streamIsCorrectlyUnzippedToCacheWhenCacheDoesNotExist()
			throws FileNotFoundException, IOException {
		URL url = dirContainingAAndBZip();
		UnmodifiableZip src = new UnmodifiableZip(url);
		network.cachesAt(src, "unzipped");

		File unzipped = iwant.unmodifiableZipUnzipped(src);

		assertEquals(testArea.root() + "/unzipped",
				unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[dir]", Arrays.toString(unzipped.list()));
		assertEquals("a\n", testArea.contentOf(new File(unzipped, "dir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "dir/b")));
	}

	@Test
	public void cacheIsReturnedWithoutUnzippingWhenCacheExists() {
		URL zip = Iwant
				.fileToUrl(new File(testArea.root(), "not-to-be-accessed"));
		File unzipped = network.cachesZipAt(zip, "unzipped");
		Iwant.textFileEnsuredToHaveContent(new File(unzipped, "file"),
				"unzipped content");

		File unzippedAgain = iwant
				.unmodifiableZipUnzipped(new UnmodifiableZip(zip));

		assertEquals(unzipped, unzippedAgain);
		assertTrue(unzipped.isDirectory());
		assertEquals("unzipped content", testArea.contentOf("unzipped/file"));
	}

	@Test
	public void unzipHandlesSubDirectoriesToo()
			throws FileNotFoundException, IOException {
		URL url = getClass().getResource("zip-with-subdir.zip");
		UnmodifiableZip src = new UnmodifiableZip(url);
		network.cachesAt(src, "unzipped");

		File unzipped = iwant.unmodifiableZipUnzipped(src);

		assertEquals(testArea.root() + "/unzipped",
				unzipped.getCanonicalPath());
		assertTrue(unzipped.isDirectory());

		assertEquals("[root]", Arrays.toString(unzipped.list()));
		assertEquals("a\n",
				testArea.contentOf(new File(unzipped, "root/subdir/a")));
		assertEquals("b\n", testArea.contentOf(new File(unzipped, "root/b")));
	}

	/**
	 * This way the error message will be seen again on later runs
	 */
	@Test
	public void unzipLeavesNoResultIfUnzipFails() throws IOException {
		File zip = new File(testArea.root(), "zip");
		UnmodifiableZip src = new UnmodifiableZip(zip.toURI().toURL());
		File cachedZip = network.cachesAt(src, "unzipped");

		testArea.fileHasContent(zip, "corrupted zip file");
		try {
			iwant.unmodifiableZipUnzipped(src);
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Corrupt (or empty, no way to tell): UnmodifiableZip:file:"
							+ zip,
					e.getMessage());
		}
		assertFalse(cachedZip.exists());
	}

}
