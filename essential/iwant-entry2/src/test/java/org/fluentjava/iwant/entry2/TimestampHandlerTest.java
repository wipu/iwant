package org.fluentjava.iwant.entry2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.entry2.Iwant2.TimestampHandler;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimestampHandlerTest {

	private TestArea testArea;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
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

	@Test
	public void refreshWhenCachedTargetWithNoSourcesIsMissing() {
		SortedSet<File> sources = new TreeSet<>();
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
	@Test
	public void existingSourceDescriptorIsDeletedIfFileNeedsRefresh() {
		SortedSet<File> sources = new TreeSet<>();
		File cachedTarget = fileIsMissing("cachedTarget");
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr", "", 1);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());

		assertFalse(sourceDescriptor.exists());
	}

	@Test
	public void fileNeedsRefreshWhenSrcDescriptorOfNoSourcesIsMissing() {
		SortedSet<File> sources = new TreeSet<>();
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 1);
		File sourceDescriptor = fileIsMissing("srcDescr");

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	@Test
	public void fileNeedsRefreshWhenSrcDescriptorContentDiffersFromCurrentSources() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				"not src1 and src2", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	@Test
	public void fileNeedsRefreshWhenOneSourceFileIsNewerThanSourceDescriptor() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 11);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				src1 + "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertTrue(timestampHandler.needsRefresh());
	}

	// refresh is not needed

	@Test
	public void existingCachedFileThatDoesNotNeedRefresh() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<>(Arrays.asList(src1, src2));
		File cachedTarget = fileHasContentAndTimestamp("cachedTarget",
				"cached content", 2);
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				src1 + "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertFalse(timestampHandler.needsRefresh());
	}

	/**
	 * Testrun does not produce any cacheable file so it uses null for it.
	 */
	@Test
	public void nullCachedFileThatDoesNotNeedRefresh() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<>(Arrays.asList(src1, src2));
		File cachedTarget = null;
		File sourceDescriptor = fileHasContentAndTimestamp("srcDescr",
				src1 + "\n" + src2 + "\n", 2);

		TimestampHandler timestampHandler = new TimestampHandler(cachedTarget,
				sourceDescriptor, sources);

		assertFalse(timestampHandler.needsRefresh());
	}

	// tests of method markFresh

	@Test
	public void markFreshWritesSortedSetOfSourceNamesToSourceDescriptor() {
		File src1 = fileHasContentAndTimestamp("src1", "whatever", 1);
		File src2 = fileHasContentAndTimestamp("src2", "whatever", 1);
		SortedSet<File> sources = new TreeSet<>(Arrays.asList(src1, src2));
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
