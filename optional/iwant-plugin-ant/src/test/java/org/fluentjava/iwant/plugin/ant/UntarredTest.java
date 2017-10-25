package org.fluentjava.iwant.plugin.ant;

import java.io.File;

import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;

public class UntarredTest extends IwantTestCase {

	public void testInregedients() {
		assertEquals("[a.tar]",
				Untarred.with().name("u").from(Source.underWsroot("a.tar"))
						.end().ingredients().toString());
		assertEquals("[b.tar.gz]",
				Untarred.with().name("u").from(Source.underWsroot("b.tar.gz"))
						.gzCompression().end().ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals(
				"org.fluentjava.iwant.plugin.ant.Untarred:"
						+ "compression=null:[a.tar]",
				Untarred.with().name("u").from(Source.underWsroot("a.tar"))
						.end().contentDescriptor());
		assertEquals(
				"org.fluentjava.iwant.plugin.ant.Untarred:"
						+ "compression=gzip:[b.tar.gz]",
				Untarred.with().name("u").from(Source.underWsroot("b.tar.gz"))
						.gzCompression().end().contentDescriptor());
	}

	public void testSuccessfullyUntarringDirAndFileTar() throws Exception {
		File tarFile = new File(getClass()
				.getResource(
						"/org/fluentjava/iwant/testresources/untarred/dir-and-file.tar")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(new ExternalSource(tarFile)).end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n", contentOfCached("untarred/dir/file"));
	}

	public void testSuccessfullyUntarringDirAndFileTarGz() throws Exception {
		File tarFile = new File(getClass()
				.getResource(
						"/org/fluentjava/iwant/testresources/untarred/dir-and-file.tar.gz")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(new ExternalSource(tarFile)).gzCompression().end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n", contentOfCached("untarred/dir/file"));
	}

}
