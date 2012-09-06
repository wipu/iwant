package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.entry.Iwant;

public class TargetEvaluationContextMock implements TargetEvaluationContext,
		CacheLocations {

	private final Iwant iwant;
	private File wsRoot;
	private File cachedModifiableTarget;
	private File cachedDescriptors;

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

	public void cachesModifiableTargetsAt(File cachedTarget) {
		this.cachedModifiableTarget = cachedTarget;
	}

	public void cachesDesciptorsAt(File cachedDescriptors) {
		this.cachedDescriptors = cachedDescriptors;
	}

	@Override
	public Iwant iwant() {
		return iwant;
	}

	@Override
	public CacheLocations cached() {
		return this;
	}

	@Override
	public File modifiableTargets() {
		return nonNull(cachedModifiableTarget, "cachedModifiableTarget");
	}

	@Override
	public File cachedDescriptors() {
		return nonNull(cachedDescriptors, "cachedDescriptorsDir");
	}

	@Override
	public File freshPathTo(Path path) {
		return path.cachedAt(this);
	}

}
