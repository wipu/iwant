package net.sf.iwant.apimocks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.testarea.TestArea;

public class SideEffectContextMock implements SideEffectContext {

	@SuppressWarnings("unused")
	private final TestArea testArea;
	private WsInfoMock wsInfo;
	private File wsRoot;
	private final TargetEvaluationContextMock evaluationCtx;
	private final List<Target> targetsWantedAsPath = new ArrayList<>();
	private File resultForTargetAsPath;
	private RuntimeException failureForIwantAsPath;
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();

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
		return err;
	}

	@Override
	public TargetEvaluationContextMock targetEvaluationContext() {
		return evaluationCtx;
	}

	public void shallReturnToIwantAsPath(File nextResultForTargetAsPath) {
		this.resultForTargetAsPath = nextResultForTargetAsPath;
	}

	public void shallFailIwantAsPathWith(
			RuntimeException failureForNextIwantAsPath) {
		this.failureForIwantAsPath = failureForNextIwantAsPath;
	}

	@Override
	public File iwantAsPath(Target target) {
		targetsWantedAsPath.add(target);
		if (failureForIwantAsPath != null) {
			throw failureForIwantAsPath;
		}
		return resultForTargetAsPath;
	}

	public List<Target> targetsWantedAsPath() {
		return targetsWantedAsPath;
	}

}
