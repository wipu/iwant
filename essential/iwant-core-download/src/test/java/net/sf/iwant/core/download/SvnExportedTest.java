package net.sf.iwant.core.download;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.iwant.apimocks.IwantTestCase;

public class SvnExportedTest extends IwantTestCase {

	public void testParallelismIsDisabledForBetterBevahiourTowardsSfnetSvnServer() {
		assertFalse(SvnExported.with().name("whatever").url(null).end()
				.supportsParallelism());
	}

	public void testIngredientsAndContentDescriptor()
			throws MalformedURLException {
		URL url = new URL("http://localhost/an-url");
		SvnExported target = SvnExported.with().name("svn-exported").url(url)
				.end();

		assertEquals("[]", target.ingredients().toString());
		assertEquals(
				"net.sf.iwant.core.download.SvnExported:http://localhost/an-url",
				target.contentDescriptor().toString());
	}

	public void testItDelegateExportToCoreServices() throws Exception {
		URL url = new URL("http://localhost/an-url");

		SvnExported target = SvnExported.with().name("svn-exported").url(url)
				.end();
		target.path(ctx);

		ctx.iwant().shallHaveSvnExportedUrlTo(url, ctx.cached(target));
	}

}
