package net.sf.iwant.apimocks;

import java.io.File;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class IwantMockEnvironment {

	private final IwantNetwork network;
	private final TestArea testArea;
	private final Iwant iwant;
	private final CachesMock caches;
	private final File cacheDir;
	private final File wsRoot;
	private final TargetEvaluationContextMock ctx;

	private IwantMockEnvironment(IwantNetwork network, TestArea testArea,
			Iwant iwant, CachesMock caches, File cacheDir, File wsRoot,
			TargetEvaluationContextMock ctx) {
		this.network = network;
		this.testArea = testArea;
		this.iwant = iwant;
		this.caches = caches;
		this.cacheDir = cacheDir;
		this.wsRoot = wsRoot;
		this.ctx = ctx;
	}

	public static IwantMockEnvironmentPlease forTest(Object test) {
		return new IwantMockEnvironmentPlease(test);
	}

	public static class IwantMockEnvironmentPlease {

		private IwantNetwork network;
		private TestArea testArea;
		private Iwant iwant;
		private CachesMock caches;
		private File cacheDir;
		private TargetEvaluationContextMock ctx;
		private File wsRoot;

		public IwantMockEnvironmentPlease(Object test) {
			this.testArea = TestArea.forTest(test);
			this.wsRoot = testArea.newDir("wsroot");
			this.network = new IwantNetworkMock(testArea);
			this.iwant = Iwant.using(network);
			this.caches = new CachesMock(testArea.root());
			this.cacheDir = testArea.newDir("caches");
			caches.cachesModifiableTargetsAt(cacheDir);
			ctx = new TargetEvaluationContextMock(iwant, caches);
		}

		public IwantMockEnvironmentPlease network(IwantNetwork network) {
			this.network = network;
			return this;
		}

		public IwantMockEnvironmentPlease iwant(Iwant iwant) {
			this.iwant = iwant;
			return this;
		}

		public IwantMockEnvironment end() {
			return new IwantMockEnvironment(network, testArea, iwant, caches,
					cacheDir, wsRoot, ctx);
		}

	}

	public IwantNetwork network() {
		return network;
	}

	public TestArea testArea() {
		return testArea;
	}

	public Iwant iwant() {
		return iwant;
	}

	public CachesMock caches() {
		return caches;
	}

	public File cacheDir() {
		return cacheDir;
	}

	public File wsRoot() {
		return wsRoot;
	}

	public TargetEvaluationContextMock ctx() {
		return ctx;
	}

}
