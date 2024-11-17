package org.fluentjava.iwant.plugin.ant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class UntarredTest extends IwantTestCase {

	@Test
	public void inregedients() {
		assertEquals("[a.tar]",
				Untarred.with().name("u").from(Source.underWsroot("a.tar"))
						.end().ingredients().toString());
		assertEquals("[b.tar.gz]",
				Untarred.with().name("u").from(Source.underWsroot("b.tar.gz"))
						.gzCompression().end().ingredients().toString());
	}

	@Test
	public void contentDescriptor() {
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

	@Test
	public void successfullyUntarringDirAndFileTar() throws Exception {
		File tarFile = new File(getClass().getResource(
				"/org/fluentjava/iwant/testresources/untarred/dir-and-file.tar")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(ExternalSource.at(tarFile)).end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n", contentOfCached(untarred, "dir/file"));
	}

	@Test
	public void successfullyUntarringDirAndFileTarGz() throws Exception {
		File tarFile = new File(getClass().getResource(
				"/org/fluentjava/iwant/testresources/untarred/dir-and-file.tar.gz")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(ExternalSource.at(tarFile)).gzCompression().end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n", contentOfCached(untarred, "dir/file"));
	}

}
