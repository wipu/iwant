package net.sf.iwant.core.download;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.iwant.apimocks.IwantTestCase;

public class SvnExportedTest extends IwantTestCase {

	private URL url;
	private SvnExported target;
	private File exported;

	@Override
	protected void moreSetUp() throws Exception {
		exported = new File(wsRoot, "exported");
	}

	public void testParallelismIsDisabledForBetterBevahiourTowardsSfnetSvnServer() {
		assertFalse(SvnExported.with().name("whatever").url(null).end()
				.supportsParallelism());
	}

	public void testIngredientsAndContentDescriptor()
			throws MalformedURLException {
		url = new URL("http://localhost/an-url");
		target = SvnExported.with().name("svn-exported").url(url).end();

		assertEquals("[]", target.ingredients().toString());
		assertEquals(
				"net.sf.iwant.core.download.SvnExported:http://localhost/an-url",
				target.contentDescriptor().toString());
	}

	/**
	 * Internal test for the test
	 */
	public void testInitiallyExportedDoesNotExist() {
		assertFalse(exported.exists());
	}

	/**
	 * Different workspaces share exports for performance
	 */
	public void testItCachesInCachedUrlLocation() throws MalformedURLException {
		url = new URL("http://localhost/an-url");
		caches.cachesUrlAt(url, exported);
		target = SvnExported.with().name("svn-exported").url(url).end();

		assertEquals(exported, ctx.cached(target));
	}

	public void testSuccessfulExport() throws Exception {
		url = new URL("http://localhost/an-url");
		caches.cachesUrlAt(url, exported);
		ctx.iwant().shallSvnExport(url, 2);

		target = SvnExported.with().name("svn-exported").url(url).end();
		target.path(ctx);

		assertTrue(exported.exists());
		assertEquals(2, exported.list().length);
		assertEquals("content of exported-0", contentOf(new File(exported,
				"exported-0")));
		assertEquals("content of exported-1", contentOf(new File(exported,
				"exported-1")));
	}

	public void testPartiallySuccessfulExportProducesNoExport()
			throws Exception {
		url = new URL("http://localhost/an-url");
		caches.cachesUrlAt(url, exported);
		ctx.iwant().shallSvnExport(url, 2);
		ctx.iwant().shallFailSvnExportAfterFileCount(url, 1);

		target = SvnExported.with().name("svn-exported").url(url).end();
		try {
			target.path(ctx);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Simulated svn export failure", e.getMessage());
		}

		assertFalse(exported.exists());
	}

	public void testRetryAfterPartialSuccessProducesExport() throws Exception {
		testPartiallySuccessfulExportProducesNoExport();

		// retry
		ctx.iwant().shallFailSvnExportAfterFileCount(url, null);
		target.path(ctx);
		assertTrue(exported.exists());
		assertEquals(2, exported.list().length);
		assertEquals("content of exported-0", contentOf(new File(exported,
				"exported-0")));
		assertEquals("content of exported-1", contentOf(new File(exported,
				"exported-1")));
	}

}
