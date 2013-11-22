package net.sf.iwant.api;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.tools.JavaCompiler;

import junit.framework.TestCase;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testing.IwantEntry3TestArea;

public class DownloadedTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private CachesMock caches;
	private File wsRoot;
	private IwantMock iwantMock;
	private TargetEvaluationContextMock ctx;
	private File cached;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		iwantMock = new IwantMock();
		ctx = new TargetEvaluationContextMock(iwantMock, caches);
		cached = new File(testArea.root(), "cachedDownload");
	}

	public void testThereAreNoIngredients() {
		assertTrue(Downloaded.withName("any").url("http://localhost/any")
				.md5("any").ingredients().isEmpty());
	}

	public void testContentDescriptor() {
		assertEquals(
				"net.sf.iwant.api.Downloaded {\n  url:http://localhost/url1\n}\n",
				Downloaded.withName("any").url("http://localhost/url1")
						.md5("any").contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.Downloaded {\n  url:http://localhost/url2\n}\n",
				Downloaded.withName("any").url("http://localhost/url2")
						.md5("any").contentDescriptor());
	}

	/**
	 * Most repositories limit the number of concurrent downloads so this is the
	 * simplest way to avoid failed downloads because of server refusing the
	 * connection.
	 */
	public void testParallelRefreshIsDisabled() {
		assertFalse(Downloaded.withName("any").url("http://any.com/file")
				.noCheck().supportsParallelism());
	}

	public void testDownloadSucceedsWithoutDigestCheck() throws Exception {
		URL url = Iwant.url("http://localhost");
		caches.cachesUrlAt(url, cached);
		iwantMock.shallDownloadContent(url, "valid content");

		Downloaded target = Downloaded.withName("any").url(url).noCheck();
		target.path(ctx);

		assertEquals("{http://localhost=" + cached + "}",
				iwantMock.executedDownloads.toString());
		assertEquals("valid content", testArea.contentOf(cached));
	}

	public void testDownloadSucceedsWithCorrectMd5() throws Exception {
		URL url = Iwant.url("http://localhost");
		caches.cachesUrlAt(url, cached);
		iwantMock.shallDownloadContent(url, "valid content");

		Downloaded target = Downloaded.withName("any").url(url)
				.md5("2cb7585162f62b8d58a09d0727faa68f");
		target.path(ctx);

		assertEquals("{http://localhost=" + cached + "}",
				iwantMock.executedDownloads.toString());
		assertEquals("valid content", testArea.contentOf(cached));
	}

	public void testDownloadFailsAndCachedFileIsRenamedIfMd5Fails()
			throws Exception {
		URL url = Iwant.url("http://localhost");
		caches.cachesUrlAt(url, cached);
		iwantMock.shallDownloadContent(url, "invalid");

		Downloaded target = Downloaded.withName("any").url(url)
				.md5("2cb7585162f62b8d58a09d0727faa68f");
		try {
			target.path(ctx);
			fail();
		} catch (Iwant.IwantException e) {
			assertEquals("Actual MD5 was fedb2d84cafe20862cb4399751a8a7e3,"
					+ " moved downloaded file to " + cached + ".corrupted",
					e.getMessage());
		}

		assertFalse(cached.exists());
		assertEquals(
				"invalid",
				testArea.contentOf(new File(cached.getCanonicalPath()
						+ ".corrupted")));
	}

	private static class IwantMock extends Iwant {

		private final Map<URL, File> executedDownloads = new LinkedHashMap<URL, File>();
		private final Map<URL, String> contentToDownload = new LinkedHashMap<URL, String>();

		IwantMock() {
			super(null);
		}

		@Override
		public IwantNetwork network() {
			return new IwantNetwork() {

				@Override
				public URL svnkitUrl() {
					throw new UnsupportedOperationException(
							"TODO test and implement");
				}

				@Override
				public URL junitUrl() {
					throw new UnsupportedOperationException(
							"TODO test and implement");
				}

				@Override
				public File cacheLocation(UnmodifiableSource<?> src) {
					throw new UnsupportedOperationException(
							"TODO test and implement");
				}

				@Override
				public JavaCompiler systemJavaCompiler() {
					throw new UnsupportedOperationException(
							"TODO test and implement");
				}

			};
		}

		@Override
		public void downloaded(URL from, File to) {
			executedDownloads.put(from, to);
			String content = contentToDownload.get(from);
			if (content == null) {
				throw new IllegalStateException(
						"You forgot to teach content of " + from);
			}
			Iwant.newTextFile(to, content);
		}

		public void shallDownloadContent(URL from, String content) {
			contentToDownload.put(from, content);
		}

		@Override
		public File downloaded(URL from) {
			fail("Not to be called");
			return null;
		}

	}

}
