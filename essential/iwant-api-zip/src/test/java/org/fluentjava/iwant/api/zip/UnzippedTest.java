package org.fluentjava.iwant.api.zip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class UnzippedTest extends IwantTestCase {

	@Test
	public void inregedients() {
		assertEquals("[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().ingredients().toString());
	}

	@Test
	public void contentDescriptor() {
		assertEquals("org.fluentjava.iwant.api.zip.Unzipped:[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().contentDescriptor());
	}

	@Test
	public void unzippedDirAndFileZip() throws Exception {
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
