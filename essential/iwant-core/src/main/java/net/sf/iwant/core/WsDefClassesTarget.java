package net.sf.iwant.core;

class WsDefClassesTarget extends Target<JavaClasses> {

	public WsDefClassesTarget(String wsName, JavaClasses content) {
		super(wsName + "-wsDefClasses", content);
	}

	/**
	 * Overriding to cache in different location than user targets, to avoid
	 * name clashes.
	 */
	@Override
	public String asAbsolutePath(Locations locations) {
		return locations.cacheDir() + "/wsDefClasses/" + name();
	}

}