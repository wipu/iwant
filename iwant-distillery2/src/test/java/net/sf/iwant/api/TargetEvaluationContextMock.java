package net.sf.iwant.api;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.iwant.entry.Iwant;

public class TargetEvaluationContextMock implements TargetEvaluationContext {

	private final Iwant iwant;
	private File wsRoot;
	private final Map<Target, File> cachedTargets = new HashMap<Target, File>();

	public TargetEvaluationContextMock(Iwant iwant) {
		this.iwant = iwant;
	}

	private <T> T nonNull(T value, Object request) {
		if (value == null) {
			throw new IllegalStateException("You forgot to teach " + request
					+ "\nto " + this);
		}
		return value;
	}

	@Override
	public File wsRoot() {
		return nonNull(wsRoot, "wsRoot");
	}

	public void hasWsRoot(File wsRoot) {
		this.wsRoot = wsRoot;
	}

	@Override
	public File freshPathTo(Target target) {
		return nonNull(cachedTargets.get(target), target);
	}

	public void cachesAt(Target target, File cachedTarget) {
		cachedTargets.put(target, cachedTarget);
	}

	@Override
	public Iwant iwant() {
		return iwant;
	}

}
