package net.sf.iwant.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;

public class DownloadedTest extends TestCase {

	public void testThereAreNowIngredients() {
		assertTrue(Downloaded.withName("any").url("http://any").md5("any")
				.ingredients().isEmpty());
	}

	public void testContentDescriptor() {
		assertEquals("net.sf.iwant.api.Downloaded {\n  url:http://url1\n}\n",
				Downloaded.withName("any").url("http://url1").md5("any")
						.contentDescriptor());
		assertEquals("net.sf.iwant.api.Downloaded {\n  url:http://url2\n}\n",
				Downloaded.withName("any").url("http://url2").md5("any")
						.contentDescriptor());
	}

	public void testCachedAtDelegatesToGivenIwant() throws Exception {
		Downloaded target = Downloaded.withName("any").url("http://an-url")
				.md5("any");
		IwantMock iwant = new IwantMock();
		iwant.cachesUrlAt("http://an-url", "downloaded");

		File actual = target.cachedAt(new TargetEvaluationContextMock(iwant));

		assertEquals(new File("downloaded"), actual);
	}

	public void testPathDelegatesDownloadingToGivenIwant() throws Exception {
		Downloaded target = Downloaded.withName("any").url("http://an-url")
				.md5("any");
		IwantMock iwant = new IwantMock();

		target.path(new TargetEvaluationContextMock(iwant));

		assertEquals("[http://an-url]", iwant.downloadedUrls.toString());
	}

	private static class IwantMock extends Iwant {

		private final Map<URL, File> taughtDownloads = new HashMap<URL, File>();
		private final List<URL> downloadedUrls = new ArrayList<URL>();

		IwantMock() {
			super(null);
		}

		public void cachesUrlAt(String url, String cached) {
			taughtDownloads.put(Iwant.url(url), new File(cached));
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
					return taughtDownloads.get(src.location());
				}

			};
		}

		@Override
		public File downloaded(URL url) {
			downloadedUrls.add(url);
			return null;
		}

	}

}
