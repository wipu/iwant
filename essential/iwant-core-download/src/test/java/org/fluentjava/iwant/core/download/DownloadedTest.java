package org.fluentjava.iwant.core.download;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.tools.JavaCompiler;

import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.apimocks.UrlString;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DownloadedTest {

	private TestArea testArea;
	private CachesMock caches;
	private File wsRoot;
	private IwantMock iwantMock;
	private TargetEvaluationContextMock ctx;
	private File cached;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		iwantMock = new IwantMock();
		ctx = new TargetEvaluationContextMock(iwantMock, caches);
		cached = new File(testArea.root(), "cachedDownload");
	}

	@Test
	public void thereAreNoIngredients() {
		assertTrue(Downloaded.withName("any").url("http://localhost/any")
				.md5("any").ingredients().isEmpty());
	}

	@Test
	public void contentDescriptor() {
		assertEquals(
				"org.fluentjava.iwant.core.download.Downloaded\n" + "p:url:\n"
						+ "  http://localhost/url1\n" + "p:md5:\n" + "  any\n"
						+ "",
				Downloaded.withName("any").url("http://localhost/url1")
						.md5("any").contentDescriptor());
		assertEquals(
				"org.fluentjava.iwant.core.download.Downloaded\n" + "p:url:\n"
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
	@Test
	public void parallelRefreshIsDisabled() {
		assertFalse(Downloaded.withName("any").url("http://any.com/file")
				.noCheck().supportsParallelism());
	}

	@Test
	public void downloadSucceedsWithoutDigestCheck() throws Exception {
		URL url = Iwant.url("http://localhost");
		caches.cachesUrlAt(url, cached);
		iwantMock.shallDownloadContent(url, "valid content");

		Downloaded target = Downloaded.withName("any").url(url).noCheck();
		target.path(ctx);

		assertEquals("{http://localhost=" + cached + "}",
				iwantMock.executedDownloads.toString());
		assertEquals("valid content", testArea.contentOf(cached));
	}

	@Test
	public void downloadSucceedsWithCorrectMd5() throws Exception {
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

	@Test
	public void downloadFailsAndCachedFileIsRenamedIfMd5Fails()
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
				public File cacheOfContentFrom(UnmodifiableSource<?> src) {
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
			Iwant.textFileEnsuredToHaveContent(to, content);
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
