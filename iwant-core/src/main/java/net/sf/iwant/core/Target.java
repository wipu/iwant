package net.sf.iwant.core;

public class Target extends Path {

	private final Content content;

	public Target(String name, Content content) {
		super(name);
		this.content = content;
	}

	public Content content() {
		return content;
	}

}
