package net.sf.iwant.core;

public class Target extends Path {

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

}
