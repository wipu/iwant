package net.sf.iwant.entry2;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.sf.iwant.entry2.Iwant2.TimestampHandler;

public class TimestampHandlerTest extends TestCase {

	private IwantEntry2TestArea testArea;

	@Override
	public void setUp() {
		testArea = new IwantEntry2TestArea();
	}

	private File fileIsMissing(String path) {
		return new File(testArea.root(), path);
	}

	private File fileHasContentAndTimestamp(String path, String content,
			long timestampInSeconds) {
		long timestamp = timestampInSeconds * 1000;
		File f = testArea.hasFile(path, content);
		f.setLastModified(timestamp);
		return f;
	}

	// tests of method needsRefresh

	// refresh is needed

	public void testRefreshWhenCachedTargetWithNoSourcesIsMissing() {
		SortedSet<File> sources = new TreeSet<File>();
		File cachedTarget = fileIsMissing("cachedTarget");
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", "", 1);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	/**
	 * Descriptor needs to be deleted anyway so let the handler do it instead of
	 * its caller.
	 */
	public void testExistingSourceDescriptorIsDeletedIfFileNeedsRefresh() {
		SortedSet<File> sources = new TreeSet<File>();
		File cachedTarget = fileIsMissing("cachedTarget");
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", "", 1);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());

		assertFalse(sourceDescriptor.exists());
	}

	public void testFileNeedsRefreshWhenSrcDescriptorOfNoSourcesIsMissing() {
		SortedSet<File> sources = new TreeSet<File>();
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 1);
		File sourceDescriptor = fileIsMissing("srcDescr");

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	public void testFileNeedsRefreshWhenSrcDescriptorContentDiffersFromCurrentSources() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<File>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				"not src1 and src2", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	public void testFileNeedsRefreshWhenOneSourceFileIsNewerThanSourceDescriptor() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 11);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<File>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", src1
				+ "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	// refresh is not needed

	public void testExistingCachedFileThatDoesNotNeedRefresh() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<File>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", src1
				+ "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertFalse(timestampHandler.needsRefresh());
	}

	/**
	 * Testrun does not produce any cacheable file so it uses null for it.
	 */
	public void testNullCachedFileThatDoesNotNeedRefresh() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<File>(Arrays.asList(src1, src2));
		File cachedTarget = null;
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", src1
				+ "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertFalse(timestampHandler.needsRefresh());
	}

	// tests of method markFresh

	public void testMarkFreshWritesSortedSetOfSourceNamesToSourceDescriptor() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<File>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				"old-src-descr", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		timestampHandler.markFresh();

		assertEquals(src1 + "\n" + src2 + "\n",
				testArea.contentOf(sourceDescriptor));
	}

}
