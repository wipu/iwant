package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.CachesMock;

public class TargetEvaluationContextMock implements TargetEvaluationContext {

	private final Iwant iwant;
	private File wsRoot;
	private final CachesMock caches;

	public TargetEvaluationContextMock(Iwant iwant, CachesMock caches) {
		this.iwant = iwant;
		this.caches = caches;
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
	public Iwant iwant() {
		return iwant;
	}

	@Override
	public File cached(Path path) {
		return caches.contentOf(path);
	}

	@Override
	public File freshTemporaryDirectory() {
		return caches.temporaryDirectory("mock-worker");
	}

}
