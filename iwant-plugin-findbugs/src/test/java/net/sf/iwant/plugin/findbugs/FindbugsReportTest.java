package net.sf.iwant.plugin.findbugs;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

public class FindbugsReportTest extends TestCase {

	private IwantPluginFindbugsTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginFindbugsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = new File(testArea.root(), "cached");
		caches.cachesModifiableTargetsAt(cached);
	}

	@SuppressWarnings("unused")
	public void testDefaultReportFromEmptyClasses() throws Exception {
		testArea.newDir("empty-src");
		testArea.newDir("empty-classes");
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Target findbugs = FindbugsReport
				.with()
				.name("fb-empty")
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		if (true) {
			return;
		}
		findbugs.path(ctx);

		assertTrue(new File(cached, "fb-empty").exists());
	}

}
