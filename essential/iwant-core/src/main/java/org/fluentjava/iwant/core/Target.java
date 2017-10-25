package org.fluentjava.iwant.core;

import java.util.SortedSet;
import java.util.TreeSet;

public class Target<CONTENT extends Content> extends Path {

	private final CONTENT content;

	public Target(String name, CONTENT content) {
		super(name);
		this.content = content;
	}

	public CONTENT content() {
		return content;
	}

	public SortedSet<Path> ingredients() {
		return content.ingredients();
	}

	public SortedSet<Target<?>> dependencies() {
		SortedSet<Target<?>> dependencies = new TreeSet<Target<?>>();
		for (Path ingredient : ingredients()) {
			if (ingredient instanceof Target) {
				dependencies.add((Target<?>) ingredient);
			}
		}
		return dependencies;
	}

	@Override
	public String asAbsolutePath(Locations locations) {
		return locations.targetCacheDir() + "/" + name();
	}

	/**
	 * TODO override when the cache obeys it, to avoid name clashes between user
	 * targets and wsDefClasses like asAbsolutePath does for the actual cached
	 * target.
	 */
	public final String contentDescriptionCacheDir(Locations locations) {
		return locations.contentDescriptionCacheDir() + "/" + name();
	}

}
