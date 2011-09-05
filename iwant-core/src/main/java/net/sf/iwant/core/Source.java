package net.sf.iwant.core;

public class Source extends Path {

	public Source(String name) {
		super(name);
	}

	@Override
	public String asAbsolutePath(Locations locations) {
		return locations.wsRoot() + "/" + name();
	}

}
