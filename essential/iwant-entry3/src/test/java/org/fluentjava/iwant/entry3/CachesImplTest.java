package org.fluentjava.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Caches;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.UnmodifiableUrl;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;

public class CachesImplTest extends TestCase {

	private TestArea testArea;
	private File cacheDir;
	private Caches caches;
	private File wsRoot;
	private IwantNetworkMock network;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
		wsRoot = testArea.newDir("wsroot");
		cacheDir = testArea.newDir("cacheDir");
		network = new IwantNetworkMock(testArea);
		caches = new CachesImpl(cacheDir, wsRoot, network);
	}

	private static void assertFile(File expected, File actual) {
		assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
	}

	public void testAskedPathCacheIsNotCreated() {
		File cached = caches.contentOf(new HelloTarget("hello", "whatever"));
		assertFalse(cached.exists());
		assertFalse(cached.getParentFile().exists());
	}

	public void testSourceIsItsOwnCacheAndRelativeToWsRoot() {
		assertFile(new File(wsRoot, "src1"),
				caches.contentOf(Source.underWsroot("src1")));
		assertFile(new File(wsRoot, "src/src2"),
				caches.contentOf(Source.underWsroot("src/src2")));
	}

	public void testExternalSourceIsItsOwnCache() {
		File file = new File("/absolute/path");
		assertFile(file, caches.contentOf(new ExternalSource(file)));
	}

	public void testHelloTargetIsCachedAtWorkspaceCache() {
		assertFile(new File(cacheDir, "target/hello"),
				caches.contentOf(new HelloTarget("hello", "whatever")));
		assertFile(new File(cacheDir, "target/hello2"),
				caches.contentOf(new HelloTarget("hello2", "whatever")));
	}

	public void testDownloadedIsCachedAtUnmodifiableUrlCache() {
		String urlString = "file:///an/url";
		URL url = Iwant.url(urlString);
		File cached = testArea.newDir("cached-url");
		network.cachesAt(new UnmodifiableUrl(url), cached);

		assertFile(cached, caches.contentOf(
				Downloaded.withName("downloaded").url(urlString).md5("any")));
	}

	public void testContentDescriptorOfNormalTarget() {
		assertFile(new File(cacheDir, "descriptor/h1"),
				caches.contentDescriptorOf(new HelloTarget("h1", "")));
		assertFile(new File(cacheDir, "descriptor/h2"),
				caches.contentDescriptorOf(new HelloTarget("h2", "")));
	}

	/**
	 * The target itself is inside workspace and so is its content descriptor,
	 * even though the cached content is reused between workspaces.
	 */
	public void testContentDescriptorOfDownloadedTarget() {
		assertFile(new File(cacheDir, "descriptor/dl1"),
				caches.contentDescriptorOf(Downloaded.withName("dl1")
						.url("file:///any").md5("any")));
		assertFile(new File(cacheDir, "descriptor/dl2"),
				caches.contentDescriptorOf(Downloaded.withName("dl2")
						.url("file:///any").md5("any")));
	}

	public void testRequestedTemporaryDirectoryExistsAndIsDirectory() {
		File tmpDir = caches.temporaryDirectory("w");
		assertTrue(tmpDir.exists());
		assertTrue(tmpDir.isDirectory());
	}

	public void testRequestedTemporaryDirectoryIsSameIfAndOnlyIfSameWorkerNameUsed() {
		File a1 = caches.temporaryDirectory("a");
		File a2 = caches.temporaryDirectory("a");
		File b = caches.temporaryDirectory("b");

		assertTrue(a1.equals(a2));
		assertFalse(a1.equals(b));
	}

	public void testTemporaryDirectoryIsEmptiedAfterPreviousUse()
			throws IOException {
		File tmpDir = caches.temporaryDirectory("w");
		File fileCreatedByPreviousWorker = new File(tmpDir,
				"file-from-previous-worker");
		fileCreatedByPreviousWorker.createNewFile();
		assertTrue(fileCreatedByPreviousWorker.exists());

		File tmpDirAgain = caches.temporaryDirectory("w");
		assertEquals(tmpDir, tmpDirAgain);

		assertFalse(fileCreatedByPreviousWorker.exists());
	}

}
