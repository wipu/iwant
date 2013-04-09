package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.entry3.IwantCoreServicesImpl;

public class TargetEvaluationContextMock implements TargetEvaluationContext {

	private File wsRoot;
	private final CachesMock caches;
	private final IwantCoreServices iwantCoreServices;

	public TargetEvaluationContextMock(Iwant iwant, CachesMock caches) {
		this.caches = caches;
		this.iwantCoreServices = new IwantCoreServicesImpl(iwant);
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
	public IwantCoreServices iwant() {
		return iwantCoreServices;
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
