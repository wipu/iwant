package net.sf.iwant.apimocks;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

public abstract class IwantTestCase extends TestCase {

	private IwantMockEnvironment e;
	protected TestArea testArea;
	protected File wsRoot;
	protected File cacheDir;
	protected TargetEvaluationContextMock ctx;
	private boolean captureOn;

	@Override
	public final void setUp() {
		e = IwantMockEnvironment.forTest(this).end();
		testArea = e.testArea();
		wsRoot = e.wsRoot();
		cacheDir = e.cacheDir();
		ctx = e.ctx();
		if (mustCaptureSystemOutAndErr()) {
			startSystemOutAndErrCapture();
		}
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

}
