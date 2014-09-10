package net.sf.iwant.apimocks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import junit.framework.TestCase;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public abstract class IwantTestCase extends TestCase {

	private IwantMockEnvironment e;
	private TestArea testArea;
	protected File wsRoot;
	/**
	 * TODO only one name
	 */
	protected File cacheDir;
	protected File cached;
	protected TargetEvaluationContextMock ctx;
	/**
	 * TODO only one name
	 */
	protected TargetEvaluationContextMock evCtx;
	protected CachesMock caches;
	protected File tmpDir;
	private boolean captureOn = false;
	protected SideEffectContextMock seCtx;
	private IwantNetworkMock network;
	private Iwant iwant;

	@Override
	public final void setUp() {
		e = IwantMockEnvironment.forTest(this).end();
		testArea = e.testArea();
		wsRoot = e.wsRoot();
		cacheDir = e.cacheDir();
		cached = cacheDir;
		ctx = e.ctx();
		evCtx = ctx;
		caches = e.caches();
		tmpDir = e.tmpDir();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		seCtx = new SideEffectContextMock(testArea,
				new TargetEvaluationContextMock(iwant, caches));
		seCtx.hasWsRoot(wsRoot);
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
		dir.mkdirs();
		return dir;
	}

	protected String contentOf(File file) {
		return testArea.contentOf(file);
	}

	protected String contentOfCached(String targetName) {
		return testArea.contentOf(new File(cached, targetName));
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

}
