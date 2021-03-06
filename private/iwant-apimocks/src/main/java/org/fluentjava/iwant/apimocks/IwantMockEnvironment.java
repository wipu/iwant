package org.fluentjava.iwant.apimocks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;

public class IwantMockEnvironment {

	private final IwantNetwork network;
	private final TestArea testArea;
	private final Iwant iwant;
	private final CachesMock caches;
	private final File cacheDir;
	private final File wsRoot;
	private final TargetEvaluationContextMock ctx;
	private final PrintStream originalOut;
	private final PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private final File tmpDir;

	private IwantMockEnvironment(IwantNetwork network, TestArea testArea,
			Iwant iwant, CachesMock caches, File cacheDir, File tmpDir,
			File wsRoot, TargetEvaluationContextMock ctx) {
		this.network = network;
		this.testArea = testArea;
		this.iwant = iwant;
		this.caches = caches;
		this.cacheDir = cacheDir;
		this.tmpDir = tmpDir;
		this.wsRoot = wsRoot;
		this.ctx = ctx;
		this.originalOut = System.out;
		this.originalErr = System.err;
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
		private File tmpDir;
		private TargetEvaluationContextMock ctx;
		private File wsRoot;

		public IwantMockEnvironmentPlease(Object test) {
			this.testArea = TestArea.forTest(test);
			this.wsRoot = testArea.newDir("wsroot");
			this.network = new IwantNetworkMock(testArea);
			this.iwant = Iwant.using(network);
			this.caches = new CachesMock(wsRoot);
			this.cacheDir = testArea.newDir("caches");
			this.caches.cachesModifiableTargetsAt(cacheDir);
			this.tmpDir = testArea.newDir("tmpDir");
			this.caches.providesTemporaryDirectoryAt(tmpDir);
			this.ctx = new TargetEvaluationContextMock(iwant, caches);
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
					cacheDir, tmpDir, wsRoot, ctx);
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

	public File tmpDir() {
		return tmpDir;
	}

	public File wsRoot() {
		return wsRoot;
	}

	public TargetEvaluationContextMock ctx() {
		return ctx;
	}

	public void startSystemOutAndErrCapture() {
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	public String out() {
		return out.toString();
	}

	public String err() {
		return err.toString();
	}

	public void restoreSystemOutAndErr() {
		System.setErr(originalErr);
		System.setOut(originalOut);
		String outStr = out();
		if (!outStr.isEmpty()) {
			System.err.print("== out:\n" + outStr);
		}
		String errStr = err();
		if (!errStr.isEmpty()) {
			System.err.print("== err:\n" + errStr);
		}
	}

}
