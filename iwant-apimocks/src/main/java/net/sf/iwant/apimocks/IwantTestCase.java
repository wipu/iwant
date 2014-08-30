package net.sf.iwant.apimocks;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

public abstract class IwantTestCase extends TestCase {

	private IwantMockEnvironment e;
	protected TestArea testArea;
	protected File wsRoot;
	/**
	 * TODO only one name
	 */
	protected File cacheDir;
	protected File cached;
	protected TargetEvaluationContextMock ctx;
	protected CachesMock caches;
	private boolean captureOn = false;

	@Override
	public final void setUp() {
		e = IwantMockEnvironment.forTest(this).end();
		testArea = e.testArea();
		wsRoot = e.wsRoot();
		cacheDir = e.cacheDir();
		cached = cacheDir;
		ctx = e.ctx();
		caches = e.caches();
		if (mustCaptureSystemOutAndErr()) {
			startSystemOutAndErrCapture();
		}
		moreSetUp();
	}

	protected void moreSetUp() {
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

	protected void wsRootHasFile(String relativePath, String content) {
		testArea.fileHasContent(new File(wsRoot, relativePath), content);
	}

	protected void wsRootHasDirectory(String relativePath) {
		new File(wsRoot, relativePath).mkdirs();
	}

	protected String contentOf(File file) {
		return testArea.contentOf(file);
	}

}
