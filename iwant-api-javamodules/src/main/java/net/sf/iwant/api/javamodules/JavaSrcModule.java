package net.sf.iwant.api.javamodules;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.StringFilter;
import net.sf.iwant.api.model.StringFilterByEquality;
import net.sf.iwant.api.model.Target;

public class JavaSrcModule extends JavaModule {

	private final String name;
	private final String locationUnderWsRoot;
	private final List<String> mainJavas;
	private final List<String> mainResources;
	private final List<String> testJavas;
	private final List<String> testResources;
	private final Set<JavaModule> mainDepsForCompilation;
	private final Set<JavaModule> mainDepsForRunOnly;
	private final Set<JavaModule> testDepsForCompilationExcludingMainDeps;
	private final Set<JavaModule> testDepsForRunOnlyExcludingMainDeps;
	private final Target generatedClasses;
	private final Target generatedSrc;
	private final CodeStylePolicy codeStylePolicy;
	private final CodeFormatterPolicy codeFormatterPolicy;
	private final List<Source> generatorSourcesToFollow;
	private final StringFilter testClassNameFilter;
	private final Charset encoding;
	private Path mainArtifact;
	private Path testArtifact;

	public JavaSrcModule(String name, String locationUnderWsRoot,
			List<String> mainJavas, List<String> mainResources,
			List<String> testJavas, List<String> testResources,
			Set<JavaModule> mainDepsForCompilation,
			Set<JavaModule> mainDepsForRunOnly,
			Set<JavaModule> testDepsForCompilationExcludingMainDeps,
			Set<JavaModule> testDepsForRunOnlyExcludingMainDeps,
			Target generatedClasses, Target generatedSrc,
			List<Source> generatorSourcesToFollow,
			CodeStylePolicy codeStylePolicy,
			CodeFormatterPolicy codeFormatterPolicy,
			StringFilter testClassNameFilter, Charset encoding,
			Set<Class<? extends JavaModuleCharacteristic>> characteristics) {
		super(characteristics);
		this.name = name;
		this.generatorSourcesToFollow = generatorSourcesToFollow;
		this.codeStylePolicy = codeStylePolicy;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJavas = mainJavas;
		this.mainResources = mainResources;
		this.testJavas = testJavas;
		this.testResources = testResources;
		this.codeFormatterPolicy = codeFormatterPolicy;
		this.testClassNameFilter = testClassNameFilter;
		this.encoding = encoding;
		this.mainDepsForCompilation = Collections
				.unmodifiableSet(mainDepsForCompilation);
		this.mainDepsForRunOnly = Collections
				.unmodifiableSet(mainDepsForRunOnly);
		this.testDepsForCompilationExcludingMainDeps = Collections
				.unmodifiableSet(testDepsForCompilationExcludingMainDeps);
		this.testDepsForRunOnlyExcludingMainDeps = Collections
				.unmodifiableSet(testDepsForRunOnlyExcludingMainDeps);
		this.generatedClasses = generatedClasses;
		this.generatedSrc = generatedSrc;
	}

	public static IwantSrcModuleSpex with() {
		return new IwantSrcModuleSpex();
	}

	public static IwantSrcModuleSpex like(JavaSrcModule m) {
		IwantSrcModuleSpex clone = with();
		clone.codeFormatter(m.codeFormatterPolicy());
		clone.codeStyle(m.codeStylePolicy());
		clone.encoding(m.encoding());
		for (Class<? extends JavaModuleCharacteristic> c : m.characteristics()) {
			clone.has(c);
		}
		clone.locationUnderWsRoot(m.locationUnderWsRoot());
		clone.mainDeps(m.mainDepsForCompilation());
		for (String mj : m.mainJavas()) {
			clone.mainJava(mj);
		}
		for (String mr : m.mainResources()) {
			clone.mainResources(mr);
		}
		clone.mainRuntimeDeps(m.mainDepsForRunOnly());
		clone.name(m.name());
		clone.testDeps(m.testDepsForCompilationExcludingMainDeps());
		clone.testedBy(m.testClassNameDefinition());
		for (String tj : m.testJavas()) {
			clone.testJava(tj);
		}
		for (String tr : m.testResources()) {
			clone.testResources(tr);
		}
		clone.testRuntimeDeps(m.testDepsForRunOnlyExcludingMainDeps());
		return clone;
	}

	public static class IwantSrcModuleSpex {

		private String name;
		private String relativeParentDir;
		private final List<String> mainJavas = new ArrayList<String>();
		private final List<String> mainResources = new ArrayList<String>();
		private final List<String> testJavas = new ArrayList<String>();
		private final List<String> testResources = new ArrayList<String>();
		private final Set<JavaModule> mainDepsForCompilation = new LinkedHashSet<JavaModule>();
		private final Set<JavaModule> mainDepsForRunOnly = new LinkedHashSet<JavaModule>();
		private final Set<JavaModule> testDepsForCompilationExcludingMainDeps = new LinkedHashSet<JavaModule>();
		private final Set<JavaModule> testDepsForRunOnlyExcludingMainDeps = new LinkedHashSet<JavaModule>();
		private Target generatedClasses;
		private Target generatedSrc;
		private final List<Source> generatorSourcesToFollow = new ArrayList<Source>();
		private CodeStylePolicy codeStylePolicy = CodeStylePolicy
				.defaultsExcept().end();
		private CodeFormatterPolicy codeFormatterPolicy = new CodeFormatterPolicy();
		private String locationUnderWsRoot;
		private StringFilter testClassNameFilter;
		private Charset encoding;
		private final Set<Class<? extends JavaModuleCharacteristic>> characteristics = new HashSet<Class<? extends JavaModuleCharacteristic>>();

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
					mainResources, testJavas, testResources,
					mainDepsForCompilation, mainDepsForRunOnly,
					testDepsForCompilationExcludingMainDeps,
					testDepsForRunOnlyExcludingMainDeps, generatedClasses,
					generatedSrc, generatorSourcesToFollow, codeStylePolicy,
					codeFormatterPolicy, testClassNameFilter, encoding,
					characteristics);
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

		public IwantSrcModuleSpex mainDeps(JavaModule... mainDepsForCompilation) {
			return mainDeps(Arrays.asList(mainDepsForCompilation));
		}

		public IwantSrcModuleSpex mainDeps(
				Collection<? extends JavaModule> mainDepsForCompilation) {
			this.mainDepsForCompilation.addAll(mainDepsForCompilation);
			return this;
		}

		public IwantSrcModuleSpex mainRuntimeDeps(
				JavaModule... mainDepsForRunOnly) {
			return mainRuntimeDeps(Arrays.asList(mainDepsForRunOnly));
		}

		public IwantSrcModuleSpex mainRuntimeDeps(
				Collection<? extends JavaModule> mainDepsForRunOnly) {
			this.mainDepsForRunOnly.addAll(mainDepsForRunOnly);
			return this;
		}

		public IwantSrcModuleSpex testDeps(
				JavaModule... testDepsForCompilationExcludingMainDeps) {
			return testDeps(Arrays
					.asList(testDepsForCompilationExcludingMainDeps));
		}

		public IwantSrcModuleSpex testDeps(
				Collection<? extends JavaModule> testDepsForCompilationExcludingMainDeps) {
			this.testDepsForCompilationExcludingMainDeps
					.addAll(testDepsForCompilationExcludingMainDeps);
			return this;
		}

		public IwantSrcModuleSpex testRuntimeDeps(
				JavaModule... testDepsForRunOnlyExcludingMainDeps) {
			return testRuntimeDeps(Arrays
					.asList(testDepsForRunOnlyExcludingMainDeps));
		}

		public IwantSrcModuleSpex testRuntimeDeps(
				Collection<? extends JavaModule> testDepsForRunOnlyExcludingMainDeps) {
			this.testDepsForRunOnlyExcludingMainDeps
					.addAll(testDepsForRunOnlyExcludingMainDeps);
			return this;
		}

		public IwantSrcModuleSpex encoding(Charset encoding) {
			this.encoding = encoding;
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
			return generatorSourcesToFollow(Arrays
					.asList(generatorSourcesToFollow));
		}

		public IwantSrcModuleSpex generatorSourcesToFollow(
				Collection<? extends Source> generatorSourcesToFollow) {
			this.generatorSourcesToFollow.addAll(generatorSourcesToFollow);
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

		public IwantSrcModuleSpex testedBy(String testSuiteName) {
			this.testClassNameFilter = new StringFilterByEquality(testSuiteName);
			return this;
		}

		public IwantSrcModuleSpex testedBy(StringFilter testClassNameFilter) {
			this.testClassNameFilter = testClassNameFilter;
			return this;
		}

		public IwantSrcModuleSpex has(
				Class<? extends JavaModuleCharacteristic> characteristic) {
			this.characteristics.add(characteristic);
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
	public Set<JavaModule> mainDepsForCompilation() {
		return mainDepsForCompilation;
	}

	@Override
	public Set<JavaModule> mainDepsForRunOnly() {
		return mainDepsForRunOnly;
	}

	@Override
	public Set<JavaModule> testDepsForCompilationExcludingMainDeps() {
		return testDepsForCompilationExcludingMainDeps;
	}

	@Override
	public Set<JavaModule> testDepsForRunOnlyExcludingMainDeps() {
		return testDepsForRunOnlyExcludingMainDeps;
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

	public Charset encoding() {
		return encoding;
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
	public synchronized Path mainArtifact() {
		if (mainArtifact == null) {
			mainArtifact = newMainArtifact();
		}
		return mainArtifact;
	}

	private Path newMainArtifact() {
		if (generatedClasses != null) {
			return generatedClasses;
		}
		if (mainJavas.isEmpty()) {
			return null;
		}
		Collection<Path> classpath = new ArrayList<Path>();
		for (JavaModule mainDep : mainDepsForCompilation()) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		return JavaClasses.with().name(name() + "-main-classes")
				.srcDirs(mainJavasAsPaths()).encoding(encoding)
				.resourceDirs(mainResourcesAsPaths()).classLocations(classpath)
				.end();
	}

	public synchronized Path testArtifact() {
		if (testArtifact == null) {
			testArtifact = newTestArtifact();
		}
		return testArtifact;
	}

	private Path newTestArtifact() {
		if (testJavas.isEmpty()) {
			return null;
		}
		Collection<Path> classpath = new ArrayList<Path>();
		Path mainClasses = mainArtifact();
		if (mainClasses != null) {
			classpath.add(mainClasses);
		}
		for (JavaModule mainDep : mainDepsForCompilation()) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		for (JavaModule testDep : testDepsForCompilationExcludingMainDeps()) {
			Path depArtifact = testDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		return JavaClasses.with().name(name() + "-test-classes")
				.srcDirs(testJavasAsPaths()).encoding(encoding)
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

	public StringFilter testClassNameDefinition() {
		return testClassNameFilter;
	}

}
