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

	@Override
	protected void setUp() {
		e = IwantMockEnvironment.forTest(this).end();
		testArea = e.testArea();
		wsRoot = e.wsRoot();
		cacheDir = e.cacheDir();
		ctx = e.ctx();
	}

}
