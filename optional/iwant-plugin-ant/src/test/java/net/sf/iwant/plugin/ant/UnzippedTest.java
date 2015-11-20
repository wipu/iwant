package net.sf.iwant.plugin.ant;

import java.io.File;

import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;

public class UnzippedTest extends IwantTestCase {

	public void testInregedients() {
		assertEquals("[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("net.sf.iwant.plugin.ant.Unzipped:[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().contentDescriptor());
	}

	public void testUnzippedDirAndFileZip() throws Exception {
		File zipFile = new File(
				getClass().getResource("unzipped-test.zip").toURI());

		Target unzipped = Unzipped.with().name("unzipped")
				.from(new ExternalSource(zipFile)).end();
		unzipped.path(ctx);

		assertTrue(new File(cached, "unzipped/file").exists());
		assertEquals("file\n", contentOfCached("unzipped/file"));
		assertTrue(new File(cached, "unzipped/dir/file-under-dir").exists());
		assertEquals("file-under-dir\n",
				contentOfCached("unzipped/dir/file-under-dir"));
	}

}
