package net.sf.iwant.api.javamodules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;

public class JavaSrcModule extends JavaModule {

	private final String name;
	private final String locationUnderWsRoot;
	private final List<String> mainJavas;
	private final List<String> mainResources;
	private final List<String> testJavas;
	private final List<String> testResources;
	private final Set<JavaModule> mainDeps;
	private final Set<JavaModule> testDeps;
	private final Target generatedClasses;
	private final Target generatedSrc;
	private final CodeStylePolicy codeStylePolicy;
	private final CodeFormatterPolicy codeFormatterPolicy;
	private final List<Source> generatorSourcesToFollow;

	public JavaSrcModule(String name, String locationUnderWsRoot,
			List<String> mainJavas, List<String> mainResources,
			List<String> testJavas, List<String> testResources,
			Set<JavaModule> mainDeps, Set<JavaModule> testDeps,
			Target generatedClasses, Target generatedSrc,
			List<Source> generatorSourcesToFollow,
			CodeStylePolicy codeStylePolicy,
			CodeFormatterPolicy codeFormatterPolicy) {
		this.name = name;
		this.generatorSourcesToFollow = generatorSourcesToFollow;
		this.codeStylePolicy = codeStylePolicy;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJavas = mainJavas;
		this.mainResources = mainResources;
		this.testJavas = testJavas;
		this.testResources = testResources;
		this.codeFormatterPolicy = codeFormatterPolicy;
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
		private final List<String> mainResources = new ArrayList<String>();
		private final List<String> testJavas = new ArrayList<String>();
		private final List<String> testResources = new ArrayList<String>();
		private final Set<JavaModule> mainDeps = new LinkedHashSet<JavaModule>();
		private final Set<JavaModule> testDeps = new LinkedHashSet<JavaModule>();
		private Target generatedClasses;
		private Target generatedSrc;
		private final List<Source> generatorSourcesToFollow = new ArrayList<Source>();
		private CodeStylePolicy codeStylePolicy = CodeStylePolicy
				.defaultsExcept().end();
		private CodeFormatterPolicy codeFormatterPolicy = new CodeFormatterPolicy();
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
					testDeps, generatedClasses, generatedSrc,
					generatorSourcesToFollow, codeStylePolicy,
					codeFormatterPolicy);
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
			this.mainResources.add(mainResources);
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
			this.testResources.add(testResources);
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

		public IwantSrcModuleSpex generatorSourcesToFollow(
				Source... generatorSourcesToFollow) {
			this.generatorSourcesToFollow.addAll(Arrays
					.asList(generatorSourcesToFollow));
			return this;
		}

		public IwantSrcModuleSpex codeStyle(CodeStylePolicy codeStylePolicy) {
			this.codeStylePolicy = codeStylePolicy;
			return this;
		}

		public IwantSrcModuleSpex codeFormatter(
				CodeFormatterPolicy codeFormatterPolicy) {
			this.codeFormatterPolicy = codeFormatterPolicy;
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

	public List<String> mainResources() {
		return mainResources;
	}

	public List<String> testJavas() {
		return testJavas;
	}

	public List<String> testResources() {
		return testResources;
	}

	@Override
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

	public CodeFormatterPolicy codeFormatterPolicy() {
		return codeFormatterPolicy;
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

	public List<Path> mainResourcesAsPaths() {
		List<Path> retval = new ArrayList<Path>();
		for (String res : mainResources) {
			retval.add(Source.underWsroot(locationUnderWsRoot() + "/" + res));
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

	public List<Path> testResourcesAsPaths() {
		List<Path> retval = new ArrayList<Path>();
		for (String res : testResources) {
			retval.add(Source.underWsroot(locationUnderWsRoot() + "/" + res));
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
				.srcDirs(mainJavasAsPaths())
				.resourceDirs(mainResourcesAsPaths()).classLocations(classpath)
				.end();
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
				.srcDirs(testJavasAsPaths())
				.resourceDirs(testResourcesAsPaths()).classLocations(classpath)
				.end();
	}

	public String locationUnderWsRoot() {
		return locationUnderWsRoot;
	}

	public String wsrootRelativeParentDir() {
		String loc = locationUnderWsRoot();
		if (loc.indexOf("/") < 0) {
			return "";
		}
		return locationUnderWsRoot.replaceFirst("/[^/]*$", "");
	}

	public List<Source> generatorSourcesToFollow() {
		return generatorSourcesToFollow;
	}

	@Override
	public String toString() {
		return name;
	}

}
