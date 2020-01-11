package org.fluentjava.iwant.api.zip;

import java.io.File;

import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;

public class UnzippedTest extends IwantTestCase {

	public void testInregedients() {
		assertEquals("[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("org.fluentjava.iwant.api.zip.Unzipped:[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().contentDescriptor());
	}

	public void testUnzippedDirAndFileZip() throws Exception {
		File zipFile = new File(
				getClass().getResource("unzipped-test.zip").toURI());

		Target unzipped = Unzipped.with().name("unzipped")
				.from(ExternalSource.at(zipFile)).end();
		unzipped.path(ctx);

		assertTrue(new File(cached, "unzipped/file").exists());
		assertEquals("file\n", contentOfCached(unzipped, "file"));
		assertTrue(new File(cached, "unzipped/dir/file-under-dir").exists());
		assertEquals("file-under-dir\n",
				contentOfCached(unzipped, "dir/file-under-dir"));
	}

}
