package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;

public class ConcatenatedTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private Iwant iwant;
	private IwantNetwork network;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock ctx;
	private File cacheDir;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		cacheDir = testArea.newDir("cache");
		caches.cachesModifiableTargetsAt(cacheDir);
		ctx = new TargetEvaluationContextMock(iwant, caches);
	}

	public void testIngredients() {
		assertEquals("[]",
				Concatenated.named("no-paths").bytes(1, 2).string("s").end()
						.ingredients().toString());
		assertEquals(
				"[src, target]",
				Concatenated.named("paths")
						.contentOf(Source.underWsroot("src"))
						.pathTo(new HelloTarget("target", "")).end()
						.ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("Concatenated {\nbytes:[1, 2]\nstring:'s'\n}\n",
				Concatenated.named("all-but-paths").bytes(1, 2).string("s")
						.end().contentDescriptor());
		assertEquals(
				"Concatenated {\ncontent-of:src\npath-of:target\n}\n" + "",
				Concatenated.named("only-paths")
						.contentOf(Source.underWsroot("src"))
						.pathTo(new HelloTarget("target", "")).end()
						.contentDescriptor());
	}

	public void testPathToEmpty() throws Exception {
		Concatenated c = Concatenated.named("empty").end();
		c.path(ctx);

		assertEquals("", testArea.contentOf(new File(cacheDir, "empty")));
	}

	public void testPathToConcatenationOfAllKinds() throws Exception {
		testArea.hasFile("src", "src-content");
		Source src = Source.underWsroot("src");
		HelloTarget target = new HelloTarget("target", "target-content");
		target.path(ctx);

		Concatenated c = Concatenated.named("all").bytes('A').bytes('B', '\n')
				.pathTo(src).string(":").contentOf(src).string("\n")
				.pathTo(target).string(":").contentOf(target).string("\n")
				.end();
		c.path(ctx);

		assertEquals("AB\n" + wsRoot + "/src:src-content\n" + cacheDir
				+ "/target:target-content\n",
				testArea.contentOf(new File(cacheDir, "all")));
	}

}
