package net.sf.iwant.api.model;

import java.io.File;

public abstract class Target implements Path {

	private final String name;

	public Target(String name) {
		this.name = name;
	}

	@Override
	public final String name() {
		return name;
	}

	public abstract void path(TargetEvaluationContext ctx) throws Exception;

	public abstract String contentDescriptor();

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return cachedAt.target(this);
	}

	@Override
	public final String toString() {
		return name();
	}

	/**
	 * Override if needed
	 */
	public boolean supportsParallelism() {
		return true;
	}

	/**
	 * Override if needed
	 */
	public boolean expectsCachedTargetMissingBeforeRefresh() {
		return true;
	}

}
