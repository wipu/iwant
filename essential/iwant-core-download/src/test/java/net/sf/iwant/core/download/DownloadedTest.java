package net.sf.iwant.core.download;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.tools.JavaCompiler;

import junit.framework.TestCase;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.apimocks.UrlString;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class DownloadedTest extends TestCase {

	private TestArea testArea;
	private CachesMock caches;
	private File wsRoot;
	private IwantMock iwantMock;
	private TargetEvaluationContextMock ctx;
	private File cached;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
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
		assertEquals("net.sf.iwant.core.download.Downloaded\n" + "p:url:\n"
				+ "  http://localhost/url1\n" + "p:md5:\n" + "  any\n" + "",
				Downloaded.withName("any").url("http://localhost/url1")
						.md5("any").contentDescriptor());
		assertEquals(
				"net.sf.iwant.core.download.Downloaded\n" + "p:url:\n"
						+ "  http://localhost/url2\n" + "p:md5:\n"
						+ "  anyother\n" + "",
				Downloaded.withName("any").url("http://localhost/url2")
						.md5("anyother").contentDescriptor());
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
		assertEquals("invalid", testArea
				.contentOf(new File(cached.getCanonicalPath() + ".corrupted")));
	}

	private static class IwantMock extends Iwant {

		private final Map<UrlString, File> executedDownloads = new LinkedHashMap<>();
		private final Map<UrlString, String> contentToDownload = new LinkedHashMap<>();

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
			executedDownloads.put(new UrlString(from), to);
			String content = contentToDownload.get(new UrlString(from));
			if (content == null) {
				throw new IllegalStateException(
						"You forgot to teach content of " + from);
			}
			Iwant.newTextFile(to, content);
		}

		public void shallDownloadContent(URL from, String content) {
			contentToDownload.put(new UrlString(from), content);
		}

		@Override
		public File downloaded(URL from) {
			fail("Not to be called");
			return null;
		}

	}

}
