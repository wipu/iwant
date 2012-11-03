package net.sf.iwant.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaModule implements Comparable<JavaModule> {

	private final String name;
	private final String locationUnderWsRoot;
	private final String mainJava;
	private final List<JavaModule> mainDeps;
	private final Path mainClasses;

	private JavaModule(String name, String locationUnderWsRoot,
			String mainJava, List<JavaModule> mainDeps, Path mainClasses) {
		this.name = name;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJava = mainJava;
		this.mainDeps = mainDeps;
		this.mainClasses = mainClasses;
	}

	public static JavaModule implicitLibrary(Path path) {
		return new JavaModule(path.name(), null, null, null, path);
	}

	public static JavaModuleSpex with() {
		return new JavaModuleSpex();
	}

	public static class JavaModuleSpex {

		private String name;
		private String locationUnderWsRoot;
		private String mainJava;
		private final List<JavaModule> mainDeps = new ArrayList<JavaModule>();

		public JavaModuleSpex name(String name) {
			this.name = name;
			return this;
		}

		public JavaModuleSpex locationUnderWsRoot(String locationUnderWsRoot) {
			this.locationUnderWsRoot = locationUnderWsRoot;
			return this;
		}

		public JavaModuleSpex mainJava(String mainJava) {
			this.mainJava = mainJava;
			return this;
		}

		public JavaModuleSpex mainDeps(JavaModule... mainDeps) {
			this.mainDeps.addAll(Arrays.asList(mainDeps));
			return this;
		}

		public JavaModule end() {
			Path srcDir = Source.underWsroot(locationUnderWsRoot + "/"
					+ mainJava);
			List<Path> classLocations = new ArrayList<Path>();
			for (JavaModule mainDep : mainDeps) {
				classLocations.add(mainDep.mainClasses());
			}
			return new JavaModule(name, locationUnderWsRoot, mainJava,
					mainDeps, new JavaClasses(name + "-main-classes", srcDir,
							classLocations));
		}

	}

	public String name() {
		return name;
	}

	public String locationUnderWsRoot() {
		return locationUnderWsRoot;
	}

	public String mainJava() {
		return mainJava;
	}

	public List<JavaModule> mainDeps() {
		return mainDeps;
	}

	public Path mainClasses() {
		return mainClasses;
	}

	public boolean isExplicit() {
		return locationUnderWsRoot != null;
	}

	@Override
	public int compareTo(JavaModule o) {
		return name().compareTo(o.name());
	}

}
