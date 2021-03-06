package org.fluentjava.iwant.apimocks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffectContext;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.entrymocks.NullCheck;
import org.fluentjava.iwant.testarea.TestArea;

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

	@Override
	public WsInfoMock wsInfo() {
		return wsInfo;
	}

	@Override
	public File wsRoot() {
		return NullCheck.nonNull(wsRoot);
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
	public File iwantFreshCached(Path target) {
		// TODO remove need to cache
		targetsWantedAsPath.add((Target) target);
		if (failureForIwantAsPath != null) {
			throw failureForIwantAsPath;
		}
		return resultForTargetAsPath;
	}

	public List<Target> targetsWantedAsPath() {
		return targetsWantedAsPath;
	}

}
