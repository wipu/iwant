package net.sf.iwant.core;

public class Source extends Path {

	public Source(String name, Locations locations) {
		super(locations.wsRoot() + "/" + name);
	}

}
