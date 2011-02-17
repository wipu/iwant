package net.sf.iwant.core;

import java.util.SortedSet;
import java.util.TreeSet;

public final class Target extends Path {

	private final Content content;
	private final String nameWithoutCacheDir;

	public Target(String name, Locations locations, Content content) {
		super(locations.targetCacheDir() + "/" + name);
		this.nameWithoutCacheDir = name;
		this.content = content;
	}

	public String nameWithoutCacheDir() {
		return nameWithoutCacheDir;
	}

	public Content content() {
		return content;
	}

	public SortedSet<Path> ingredients() {
		return content.ingredients();
	}

	public SortedSet<Target> dependencies() {
		SortedSet<Target> dependencies = new TreeSet();
		for (Path ingredient : ingredients()) {
			if (ingredient instanceof Target) {
				dependencies.add((Target) ingredient);
			}
		}
		return dependencies;
	}

}
