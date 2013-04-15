package net.sf.iwant.api;

import java.io.File;
import java.io.OutputStream;

import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.testarea.TestArea;

public class SideEffectContextMock implements SideEffectContext {

	@SuppressWarnings("unused")
	private final TestArea testArea;
	private WsInfoMock wsInfo;
	private File wsRoot;
	private final TargetEvaluationContextMock evaluationCtx;

	public SideEffectContextMock(TestArea testArea,
			TargetEvaluationContextMock evaluationCtx) {
		this.testArea = testArea;
		this.evaluationCtx = evaluationCtx;
		this.wsInfo = new WsInfoMock();
	}

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

	@Override
	public WsInfoMock wsInfo() {
		return wsInfo;
	}

	@Override
	public File wsRoot() {
		return nonNull(wsRoot, "wsRoot");
	}

	public void hasWsRoot(File wsRoot) {
		this.wsRoot = wsRoot;
		evaluationCtx.hasWsRoot(wsRoot);
	}

	@Override
	public OutputStream err() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public TargetEvaluationContextMock targetEvaluationContext() {
		return evaluationCtx;
	}

}
