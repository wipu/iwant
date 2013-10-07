package net.sf.iwant.plugin.ant;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

public class UntarredTest extends TestCase {

	private IwantPluginAntTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginAntTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cached);
	}

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
