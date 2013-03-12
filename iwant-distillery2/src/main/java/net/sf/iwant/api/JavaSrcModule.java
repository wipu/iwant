package net.sf.iwant.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class JavaSrcModule extends JavaModule {

	private final String name;
	private final String locationUnderWsRoot;
	private final String mainJava;
	private final String mainResources;
	private final String testJava;
	private final String testResources;
	private final Set<JavaModule> mainDeps;
	private final Set<JavaModule> testDeps;
	private final Target generatedClasses;
	private final Target generatedSrc;
	private final CodeStylePolicy codeStylePolicy;

	public JavaSrcModule(String name, String locationUnderWsRoot,
			String mainJava, String mainResources, String testJava,
			String testResources, Set<JavaModule> mainDeps,
			Set<JavaModule> testDeps, Target generatedClasses,
			Target generatedSrc, CodeStylePolicy codeStylePolicy) {
		this.name = name;
		this.codeStylePolicy = codeStylePolicy;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJava = mainJava;
		this.mainResources = mainResources;
		this.testJava = testJava;
		this.testResources = testResources;
		this.mainDeps = Collections.unmodifiableSet(mainDeps);
		this.testDeps = Collections.unmodifiableSet(testDeps);
		this.generatedClasses = generatedClasses;
		this.generatedSrc = generatedSrc;
	}

	public static IwantSrcModuleSpex with() {
		return new IwantSrcModuleSpex();
	}

	public static class IwantSrcModuleSpex {

		private String name;
		private String relativeParentDir;
		private String mainJava;
		private String mainResources;
		private String testJava;
		private String testResources;
		private final Set<JavaModule> mainDeps = new LinkedHashSet<JavaModule>();
		private final Set<JavaModule> testDeps = new LinkedHashSet<JavaModule>();
		private Target generatedClasses;
		private Target generatedSrc;
		private CodeStylePolicy codeStylePolicy = CodeStylePolicy
				.defaultsExcept().end();
		private String locationUnderWsRoot;

		public JavaSrcModule end() {
			if (locationUnderWsRoot != null && relativeParentDir != null) {
				throw new IllegalArgumentException(
						"You must not specify both relativeParentDir and locationUnderWsRoot");
			}
			String locationUnderWsRootToUse;
			if (locationUnderWsRoot != null) {
				locationUnderWsRootToUse = locationUnderWsRoot;
			} else {
				locationUnderWsRootToUse = normalizedRelativeParentDir(relativeParentDir)
						+ name;
			}
			return new JavaSrcModule(name, locationUnderWsRootToUse, mainJava,
					mainResources, testJava, testResources, mainDeps, testDeps,
					generatedClasses, generatedSrc, codeStylePolicy);
		}

		private static String normalizedRelativeParentDir(String value) {
			if (value == null || value.isEmpty()) {
				return "";
			}
			if (!value.endsWith("/")) {
				return value + "/";
			}
			return value;
		}

		public IwantSrcModuleSpex name(String name) {
			this.name = name;
			return this;
		}

		public IwantSrcModuleSpex relativeParentDir(String relativeParentDir) {
			this.relativeParentDir = relativeParentDir;
			return this;
		}

		public IwantSrcModuleSpex locationUnderWsRoot(String locationUnderWsRoot) {
			this.locationUnderWsRoot = locationUnderWsRoot;
			return this;
		}

		public IwantSrcModuleSpex mavenLayout() {
			return mainJava("src/main/java")
					.mainResources("src/main/resources")
					.testJava("src/test/java")
					.testResources("src/test/resources");
		}

		public IwantSrcModuleSpex mainJava(String mainJava) {
			this.mainJava = mainJava;
			return this;
		}

		public IwantSrcModuleSpex mainResources(String mainResources) {
			this.mainResources = mainResources;
			return this;
		}

		public IwantSrcModuleSpex testJava(String testJava) {
			this.testJava = testJava;
			return this;
		}

		public IwantSrcModuleSpex testResources(String testResources) {
			this.testResources = testResources;
			return this;
		}

		public IwantSrcModuleSpex mainDeps(JavaModule... mainDeps) {
			for (JavaModule mainDep : mainDeps) {
				this.mainDeps.add(mainDep);
			}
			return this;
		}

		public IwantSrcModuleSpex testDeps(JavaModule... testDeps) {
			for (JavaModule testDep : testDeps) {
				this.testDeps.add(testDep);
			}
			return this;
		}

		public IwantSrcModuleSpex exportsClasses(Target generatedClasses,
				Target generatedSrc) {
			this.generatedClasses = generatedClasses;
			this.generatedSrc = generatedSrc;
			return this;
		}

		public IwantSrcModuleSpex codeStyle(CodeStylePolicy codeStylePolicy) {
			this.codeStylePolicy = codeStylePolicy;
			return this;
		}

	}

	@Override
	public String name() {
		return name;
	}

	public String mainJava() {
		return mainJava;
	}

	public String mainResources() {
		return mainResources;
	}

	public String testJava() {
		return testJava;
	}

	public String testResources() {
		return testResources;
	}

	public Set<JavaModule> mainDeps() {
		return mainDeps;
	}

	public Set<JavaModule> testDeps() {
		return testDeps;
	}

	public Target generatedClasses() {
		return generatedClasses;
	}

	public Target generatedSrc() {
		return generatedSrc;
	}

	public CodeStylePolicy codeStylePolicy() {
		return codeStylePolicy;
	}

	public String relativeWsRoot() {
		return locationUnderWsRoot.replaceAll("[^/]+", "..");
	}

	public Path mainJavaAsPath() {
		if (generatedSrc != null) {
			return generatedSrc;
		}
		if (mainJava == null) {
			return null;
		}
		return Source.underWsroot(locationUnderWsRoot() + "/" + mainJava);
	}

	@Override
	public Path mainArtifact() {
		if (generatedClasses != null) {
			return generatedClasses;
		}
		if (mainJava == null) {
			return null;
		}
		Collection<Path> classpath = new ArrayList<Path>();
		for (JavaModule mainDep : mainDeps) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		return new JavaClasses(name() + "-main-classes", mainJavaAsPath(),
				classpath);
	}

	public String locationUnderWsRoot() {
		return locationUnderWsRoot;
	}

	@Override
	public String toString() {
		return name;
	}

}
