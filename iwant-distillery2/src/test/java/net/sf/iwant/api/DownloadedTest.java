package net.sf.iwant.api;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;

public class DownloadedTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private CachesMock caches;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
	}

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

	public void testPathDelegatesDownloadingToGivenIwant() throws Exception {
		String url = "http://an-url";
		IwantMock iwant = new IwantMock();
		File cached = testArea.newDir("downloaded");
		caches.cachesUrlAt(Iwant.url(url), cached);

		Downloaded target = Downloaded.withName("any").url(url).md5("any");
		target.path(new TargetEvaluationContextMock(iwant, caches));

		assertEquals("{http://an-url=" + cached + "}",
				iwant.executedDownloads.toString());
	}

	private static class IwantMock extends Iwant {

		private final Map<URL, File> executedDownloads = new LinkedHashMap<URL, File>();

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

			};
		}

		@Override
		public void downloaded(URL from, File to) {
			executedDownloads.put(from, to);
		}

		@Override
		public File downloaded(URL from) {
			fail("Not to be called");
			return null;
		}

	}

}
