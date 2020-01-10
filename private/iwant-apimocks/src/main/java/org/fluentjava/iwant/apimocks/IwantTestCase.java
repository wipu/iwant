package org.fluentjava.iwant.apimocks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;

import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.coreservices.IwantCoreServicesImpl;
import org.fluentjava.iwant.coreservices.StreamUtil;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

public abstract class IwantTestCase extends TestCase {

	private IwantMockEnvironment e;
	private TestArea testArea;
	protected File wsRoot;
	protected File cached;
	protected TargetEvaluationContextMock ctx;
	protected CachesMock caches;
	protected File tmpDir;
	private boolean captureOn = false;
	protected SideEffectContextMock seCtx;
	private IwantNetworkMock network;
	private Iwant iwant;
	private IwantCoreServices realCoreServices;

	@Override
	public final void setUp() throws Exception {
		e = IwantMockEnvironment.forTest(this).end();
		testArea = e.testArea();
		wsRoot = e.wsRoot();
		cached = e.cacheDir();
		ctx = e.ctx();
		caches = e.caches();
		tmpDir = e.tmpDir();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		seCtx = new SideEffectContextMock(testArea,
				new TargetEvaluationContextMock(iwant, caches));
		seCtx.hasWsRoot(wsRoot);
		realCoreServices = new IwantCoreServicesImpl(iwant);
		if (mustCaptureSystemOutAndErr()) {
			startSystemOutAndErrCapture();
		}
		moreSetUp();
	}

	protected void moreSetUp() throws Exception {
		// override if needed
	}

	@Override
	public final void tearDown() throws Exception {
		if (captureOn) {
			restoreSystemOutAndErr();
		}
	}

	protected boolean mustCaptureSystemOutAndErr() {
		return true;
	}

	private void startSystemOutAndErrCapture() {
		e.startSystemOutAndErrCapture();
		captureOn = true;
	}

	private void restoreSystemOutAndErr() {
		e.restoreSystemOutAndErr();
		captureOn = false;
	}

	protected String out() {
		return e.out();
	}

	protected String err() {
		return e.err();
	}

	protected File wsRootHasFile(String relativePath, String content) {
		return testArea.fileHasContent(new File(wsRoot, relativePath), content);
	}

	protected void wsRootHasFile(String relativePath, byte[] content) {
		try {
			StreamUtil.pipeAndClose(new ByteArrayInputStream(content),
					new FileOutputStream(new File(wsRoot, relativePath)));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	protected File wsRootHasDirectory(String relativePath) {
		File dir = new File(wsRoot, relativePath);
		Iwant.mkdirs(dir);
		return dir;
	}

	protected String contentOf(File file) {
		return testArea.contentOf(file);
	}

	protected String contentOfCached(Path path) {
		return contentOf(ctx.cached(path));
	}

	protected String contentOfCached(Path path, String relpath) {
		return contentOf(new File(ctx.cached(path), relpath));
	}

	protected String contentOfFileUnderWsRoot(String relativePath) {
		return testArea.contentOf(new File(wsRoot, relativePath));
	}

	protected File anExistingDirectory(String path) {
		return testArea.newDir("misc/" + path);
	}

	protected String unixPathOf(File file) {
		return ctx.iwant().unixPathOf(file);
	}

	/**
	 * Keeping tests green on windows
	 */
	protected String slashed(File file) {
		return realCoreServices.pathWithoutBackslashes(file);
	}

	protected void cacheProvidesRealDownloaded(URL url) {
		caches.cachesUrlAt(url, Iwant.usingRealNetwork().downloaded(url));
	}

}
