package net.sf.iwant.core;

public class Locations {

	private final String wsRoot;
	private final String cacheDir;

	public Locations(String wsRoot, String cacheDir) {
		this.wsRoot = wsRoot;
		this.cacheDir = cacheDir;
	}

	public String wsRoot() {
		return wsRoot;
	}

	public String cacheDir() {
		return cacheDir;
	}

}