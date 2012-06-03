package net.sf.iwant.api;

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

	/**
	 * Override if really needed
	 */
	@Override
	public File cachedAt(CacheLocations cached) {
		return new File(cached.modifiableTargets(), name());
	}

}
