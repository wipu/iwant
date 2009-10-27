package net.sf.iwant.core;

public class Target extends Path {

	private final Content content;

	public Target(String name, Locations locations, Content content) {
		super(locations.cacheDir() + "/" + name);
		this.content = content;
	}

	public Content content() {
		return content;
	}

}
