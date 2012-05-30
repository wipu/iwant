package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry3.IwantEntry3TestArea;

public class TargetEvaluationContextMock implements TargetEvaluationContext {

	private final File cached;

	public TargetEvaluationContextMock(IwantEntry3TestArea testArea) {
		cached = testArea.newDir("mock-cached/target");
	}

	@Override
	public File wsRoot() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File freshPathTo(Target target) {
		return new File(cached, target.name());
	}

}
