package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.ExternalSource;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.Source;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.UnmodifiableUrl;
import net.sf.iwant.entry.IwantNetworkMock;

public class CachesImplTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File asSomeone;
	private Caches caches;
	private File wsRoot;
	private IwantNetworkMock network;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		wsRoot = testArea.newDir("wsroot");
		asSomeone = testArea.newDir("as-someone");
		network = new IwantNetworkMock(testArea);
		caches = new CachesImpl(asSomeone, wsRoot, network);
	}

	private static void assertFile(File expected, File actual) {
		assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
	}

	public void testAskedPathCacheIsNotCreated() {
		File cached = caches.contentOf(new HelloTarget("hello", "whatever"));
		assertFalse(cached.exists());
		assertFalse(cached.getParentFile().exists());
	}

	public void testWorkspaceCacheRoot() {
		assertFile(new File(asSomeone, ".i-cached"),
				((CachesImpl) caches).wsCache());
	}

	public void testSourceIsItsOwnCacheAndRelativeToWsRoot() {
		assertFile(new File(wsRoot, "src1"),
				caches.contentOf(Source.underWsroot("src1")));
		assertFile(new File(wsRoot, "src/src2"),
				caches.contentOf(Source.underWsroot("src/src2")));
	}

	public void testExternalSourceIsItsOwnCache() throws IOException {
		File file = new File("/absolute/path");
		assertFile(file, caches.contentOf(new ExternalSource(file)));
	}

	public void testHelloTargetIsCachedAtWorkspaceCache() {
		assertFile(new File(asSomeone, ".i-cached/target/hello"),
				caches.contentOf(new HelloTarget("hello", "whatever")));
		assertFile(new File(asSomeone, ".i-cached/target/hello2"),
				caches.contentOf(new HelloTarget("hello2", "whatever")));
	}

	public void testDownloadedIsCachedAtUnmodifiableUrlCache() {
		String urlString = "file:///an/url";
		URL url = Iwant.url(urlString);
		File cached = testArea.newDir("cached-url");
		network.cachesAt(new UnmodifiableUrl(url), cached);

		assertFile(
				cached,
				caches.contentOf(Downloaded.withName("downloaded")
						.url(urlString).md5("any")));
	}

	public void testContentDescriptorOfNormalTarget() {
		assertFile(new File(asSomeone, ".i-cached/descriptor/h1"),
				caches.contentDescriptorOf(new HelloTarget("h1", "")));
		assertFile(new File(asSomeone, ".i-cached/descriptor/h2"),
				caches.contentDescriptorOf(new HelloTarget("h2", "")));
	}

	/**
	 * The target itself is inside workspace and so is its content descriptor,
	 * even though the cached content is reused between workspaces.
	 */
	public void testContentDescriptorOfDownloadedTarget() {
		assertFile(
				new File(asSomeone, ".i-cached/descriptor/dl1"),
				caches.contentDescriptorOf(Downloaded.withName("dl1")
						.url("file:///any").md5("any")));
		assertFile(
				new File(asSomeone, ".i-cached/descriptor/dl2"),
				caches.contentDescriptorOf(Downloaded.withName("dl2")
						.url("file:///any").md5("any")));
	}

}
