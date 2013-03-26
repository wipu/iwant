package net.sf.iwant.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JavaSrcModule extends JavaModule {

	private final String name;
	private final String locationUnderWsRoot;
	private final List<String> mainJavas;
	private final String mainResources;
	private final List<String> testJavas;
	private final String testResources;
	private final Set<JavaModule> mainDeps;
	private final Set<JavaModule> testDeps;
	private final Target generatedClasses;
	private final Target generatedSrc;
	private final CodeStylePolicy codeStylePolicy;

	public JavaSrcModule(String name, String locationUnderWsRoot,
			List<String> mainJavas, String mainResources,
			List<String> testJavas, String testResources,
			Set<JavaModule> mainDeps, Set<JavaModule> testDeps,
			Target generatedClasses, Target generatedSrc,
			CodeStylePolicy codeStylePolicy) {
		this.name = name;
		this.codeStylePolicy = codeStylePolicy;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJavas = mainJavas;
		this.mainResources = mainResources;
		this.testJavas = testJavas;
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
		private final List<String> mainJavas = new ArrayList<String>();
		private String mainResources;
		private final List<String> testJavas = new ArrayList<String>();
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
			return new JavaSrcModule(name, locationUnderWsRootToUse, mainJavas,
					mainResources, testJavas, testResources, mainDeps,
					testDeps, generatedClasses, generatedSrc, codeStylePolicy);
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
			this.mainJavas.add(mainJava);
			return this;
		}

		public IwantSrcModuleSpex noMainJava() {
			this.mainJavas.clear();
			return this;
		}

		public IwantSrcModuleSpex mainResources(String mainResources) {
			this.mainResources = mainResources;
			return this;
		}

		public IwantSrcModuleSpex testJava(String testJava) {
			this.testJavas.add(testJava);
			return this;
		}

		public IwantSrcModuleSpex noTestJava() {
			this.testJavas.clear();
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

	public List<String> mainJavas() {
		return mainJavas;
	}

	public String mainResources() {
		return mainResources;
	}

	public List<String> testJavas() {
		return testJavas;
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

	public List<Path> mainJavasAsPaths() {
		if (generatedSrc != null) {
			return Collections.<Path> singletonList(generatedSrc);
		}
		List<Path> retval = new ArrayList<Path>();
		for (String mainJava : mainJavas) {
			retval.add(Source.underWsroot(locationUnderWsRoot() + "/"
					+ mainJava));
		}
		return retval;
	}

	public List<Path> testJavasAsPaths() {
		List<Path> retval = new ArrayList<Path>();
		for (String testJava : testJavas) {
			retval.add(Source.underWsroot(locationUnderWsRoot() + "/"
					+ testJava));
		}
		return retval;
	}

	@Override
	public Path mainArtifact() {
		if (generatedClasses != null) {
			return generatedClasses;
		}
		if (mainJavas.isEmpty()) {
			return null;
		}
		Collection<Path> classpath = new ArrayList<Path>();
		for (JavaModule mainDep : mainDeps) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		return JavaClasses.with().name(name() + "-main-classes")
				.srcDirs(mainJavasAsPaths()).classLocations(classpath).end();
	}

	public Path testArtifact() {
		if (testJavas.isEmpty()) {
			return null;
		}
		Collection<Path> classpath = new ArrayList<Path>();
		Path mainClasses = mainArtifact();
		if (mainClasses != null) {
			classpath.add(mainClasses);
		}
		for (JavaModule mainDep : mainDeps) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		for (JavaModule testDep : testDeps) {
			Path depArtifact = testDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		return JavaClasses.with().name(name() + "-test-classes")
				.srcDirs(testJavasAsPaths()).classLocations(classpath).end();
	}

	public String locationUnderWsRoot() {
		return locationUnderWsRoot;
	}

	@Override
	public String toString() {
		return name;
	}

}
