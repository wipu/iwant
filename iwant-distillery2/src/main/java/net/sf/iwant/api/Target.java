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

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return cachedAt.target(this);
	}

	@Override
	public final String toString() {
		return name();
	}

}
