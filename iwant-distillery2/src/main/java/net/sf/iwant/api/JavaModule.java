package net.sf.iwant.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaModule implements Comparable<JavaModule> {

	private final String name;
	private final String locationUnderWsRoot;
	private final String mainJava;
	private final List<JavaModule> mainDeps;
	private final Path mainClasses;
	private final String testJava;
	private final List<JavaModule> testDeps;
	private final Path testClasses;

	public JavaModule(String name, String locationUnderWsRoot, String mainJava,
			List<JavaModule> mainDeps, Path mainClasses, String testJava,
			List<JavaModule> testDeps, Path testClasses) {
		this.name = name;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJava = mainJava;
		this.mainDeps = mainDeps;
		this.mainClasses = mainClasses;
		this.testJava = testJava;
		this.testDeps = testDeps;
		this.testClasses = testClasses;
	}

	public static JavaModule implicitLibrary(Path path) {
		List<JavaModule> depModules = new ArrayList<JavaModule>();
		for (Path dep : path.ingredients()) {
			JavaModule depModule = JavaModule.implicitLibrary(dep);
			depModules.add(depModule);
		}
		return new JavaModule(path.name(), null, null, depModules, path, null,
				Collections.<JavaModule> emptyList(), null);
	}

	public static JavaModuleSpex with() {
		return new JavaModuleSpex();
	}

	public static class JavaModuleSpex {

		private String name;
		private String locationUnderWsRoot;
		private String mainJava;
		private String testJava;
		private final List<JavaModule> mainDeps = new ArrayList<JavaModule>();
		private final List<JavaModule> testDeps = new ArrayList<JavaModule>();

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

		public JavaModuleSpex testJava(String testJava) {
			this.testJava = testJava;
			return this;
		}

		public JavaModuleSpex mainDeps(JavaModule... mainDeps) {
			this.mainDeps.addAll(Arrays.asList(mainDeps));
			return this;
		}

		public JavaModuleSpex testDeps(JavaModule... testDeps) {
			this.testDeps.addAll(Arrays.asList(testDeps));
			return this;
		}

		public JavaModule end() {
			List<JavaModule> effectiveTestDeps = new ArrayList<JavaModule>(
					testDeps);
			for (JavaModule mainDep : mainDeps) {
				if (!effectiveTestDeps.contains(mainDep)) {
					effectiveTestDeps.add(mainDep);
				}
			}
			JavaClasses mainClassesTarget = newClassesTarget("main", mainJava,
					mainDeps);
			return new JavaModule(name, locationUnderWsRoot, mainJava,
					mainDeps, mainClassesTarget, testJava, testDeps,
					newClassesTarget("test", testJava, effectiveTestDeps,
							mainClassesTarget));
		}

		private JavaClasses newClassesTarget(String type,
				String relativeJavaDir, List<JavaModule> depModules,
				Path... depPaths) {
			if (relativeJavaDir == null) {
				return null;
			}
			Path srcDir = Source.underWsroot(locationUnderWsRoot + "/"
					+ relativeJavaDir);
			List<Path> classLocations = new ArrayList<Path>();
			for (JavaModule depModule : depModules) {
				Path depMainClasses = depModule.mainClasses();
				if (depMainClasses != null) {
					classLocations.add(depMainClasses);
				}
			}
			for (Path depPath : depPaths) {
				if (depPath == null) {
					continue;
				}
				classLocations.add(depPath);
			}
			JavaClasses mainClasses = new JavaClasses(name + "-" + type
					+ "-classes", srcDir, classLocations);
			return mainClasses;
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

	public String testJava() {
		return testJava;
	}

	public List<JavaModule> mainDeps() {
		return mainDeps;
	}

	public List<JavaModule> testDeps() {
		return testDeps;
	}

	public Path mainClasses() {
		return mainClasses;
	}

	public Path testClasses() {
		return testClasses;
	}

	public boolean isExplicit() {
		return locationUnderWsRoot != null;
	}

	@Override
	public int compareTo(JavaModule o) {
		return name().compareTo(o.name());
	}

}
