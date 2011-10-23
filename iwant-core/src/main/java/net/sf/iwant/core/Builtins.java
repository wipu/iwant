package net.sf.iwant.core;

import java.util.SortedSet;
import java.util.TreeSet;

public class Builtins extends ContainerPath {

	public Builtins(Locations locations) {
		super("iwant", locations);
	}

	public SortedSet<Path> all() {
		SortedSet<Path> all = new TreeSet<Path>();
		all.add(ant171classes());
		all.add(antJunit171classes());
		all.add(junit381Classes());
		all.add(iwantCoreClasses());
		return all;
	}

	public Path ant171classes() {
		return builtInClasses("ant-1.7.1.jar");
	}

	public Path antJunit171classes() {
		return builtInClasses("ant-junit-1.7.1.jar");
	}

	public Path junit381Classes() {
		return builtInClasses("junit-3.8.1.jar");
	}

	public Path iwantCoreClasses() {
		return builtInClasses("iwant-core");
	}

	private Path builtInClasses(String name) {
		return new BuiltinPath(name);
	}

	private class BuiltinPath extends Path {

		public BuiltinPath(String name) {
			super(name);
		}

		@Override
		public String asAbsolutePath(Locations locations) {
			return locations.iwantLibs() + "/" + name();
		}

	}

}
