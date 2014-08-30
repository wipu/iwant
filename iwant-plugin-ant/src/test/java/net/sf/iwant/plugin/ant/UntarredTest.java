package net.sf.iwant.plugin.ant;

import java.io.File;

import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;

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
		assertEquals("net.sf.iwant.plugin.ant.Untarred:"
				+ "compression=null:[a.tar]",
				Untarred.with().name("u").from(Source.underWsroot("a.tar"))
						.end().contentDescriptor());
		assertEquals("net.sf.iwant.plugin.ant.Untarred:"
				+ "compression=gzip:[b.tar.gz]", Untarred.with().name("u")
				.from(Source.underWsroot("b.tar.gz")).gzCompression().end()
				.contentDescriptor());
	}

	public void testSuccessfullyUntarringDirAndFileTar() throws Exception {
		File tarFile = new File(getClass().getResource(
				"/net/sf/iwant/testresources/untarred/dir-and-file.tar")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(new ExternalSource(tarFile)).end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n",
				testArea.contentOf(new File(cached, "untarred/dir/file")));
	}

	public void testSuccessfullyUntarringDirAndFileTarGz() throws Exception {
		File tarFile = new File(getClass().getResource(
				"/net/sf/iwant/testresources/untarred/dir-and-file.tar.gz")
				.toURI());

		Target untarred = Untarred.with().name("untarred")
				.from(new ExternalSource(tarFile)).gzCompression().end();
		untarred.path(ctx);

		assertTrue(new File(cached, "untarred/dir").exists());
		assertEquals("file content\n",
				testArea.contentOf(new File(cached, "untarred/dir/file")));
	}

}
