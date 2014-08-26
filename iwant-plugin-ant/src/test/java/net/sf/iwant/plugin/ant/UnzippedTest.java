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
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class UnzippedTest extends TestCase {

	private TestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
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
		assertEquals("[a.zip]",
				Unzipped.with().name("u").from(Source.underWsroot("a.zip"))
						.end().ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("net.sf.iwant.plugin.ant.Unzipped:[a.zip]", Unzipped
				.with().name("u").from(Source.underWsroot("a.zip")).end()
				.contentDescriptor());
	}

	public void testUnzippedDirAndFileZip() throws Exception {
		File zipFile = new File(getClass().getResource("unzipped-test.zip")
				.toURI());

		Target unzipped = Unzipped.with().name("unzipped")
				.from(new ExternalSource(zipFile)).end();
		unzipped.path(ctx);

		assertTrue(new File(cached, "unzipped/file").exists());
		assertEquals("file\n",
				testArea.contentOf(new File(cached, "unzipped/file")));
		assertTrue(new File(cached, "unzipped/dir/file-under-dir").exists());
		assertEquals("file-under-dir\n", testArea.contentOf(new File(cached,
				"unzipped/dir/file-under-dir")));
	}

}
