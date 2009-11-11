package net.sf.iwant.core;

public class Builtins extends ContainerPath {

	public Builtins(Locations locations) {
		super("iwant", locations);
	}

	public Path junit381Classes() {
		return builtInClasses("junit-3.8.1.jar");
	}

	private Path builtInClasses(String name) {
		return new BuiltinPath("cpitems/" + name);
	}

	private class BuiltinPath extends Path {

		public BuiltinPath(String name) {
			super(locations.targetCacheDir() + "/../../iwant/" + name);
		}

	}

}
