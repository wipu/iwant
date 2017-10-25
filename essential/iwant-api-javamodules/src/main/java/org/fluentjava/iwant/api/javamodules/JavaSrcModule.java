package org.fluentjava.iwant.api.javamodules;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fluentjava.iwant.api.core.StringFilterByEquality;
import org.fluentjava.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.api.model.SystemEnv;
import org.fluentjava.iwant.api.model.Target;

public class JavaSrcModule extends JavaModule {

	private final String name;
	private final String locationUnderWsRoot;
	private final List<String> mainJavas;
	private final List<String> mainResources;
	private final List<String> testJavas;
	private final List<String> testResources;
	private final SystemEnv testEnv;
	private final Set<JavaModule> mainDepsForCompilation;
	private final Set<JavaModule> mainDepsForRunOnly;
	private final Set<JavaModule> testDepsForCompilationExcludingMainDeps;
	private final Set<JavaModule> testDepsForRunOnlyExcludingMainDeps;
	private final Target generatedClasses;
	private final Target generatedSrc;
	private final CodeStylePolicy codeStylePolicy;
	private final CodeFormatterPolicy codeFormatterPolicy;
	private final JavaCompliance javaCompliance;
	private final List<Source> generatorSourcesToFollow;
	private final StringFilter testClassNameFilter;
	private final Charset encoding;
	private final List<String> rawCompilerArgs;
	private final ScalaVersion scalaVersion;
	private Path mainArtifact;
	private Path testArtifact;
	private TestRunner testRunner;

	public JavaSrcModule(String name, String locationUnderWsRoot,
			List<String> mainJavas, List<String> mainResources,
			List<String> testJavas, List<String> testResources,
			SystemEnv testEnv, Set<JavaModule> mainDepsForCompilation,
			Set<JavaModule> mainDepsForRunOnly,
			Set<JavaModule> testDepsForCompilationExcludingMainDeps,
			Set<JavaModule> testDepsForRunOnlyExcludingMainDeps,
			Target generatedClasses, Target generatedSrc,
			List<Source> generatorSourcesToFollow,
			CodeStylePolicy codeStylePolicy,
			CodeFormatterPolicy codeFormatterPolicy,
			JavaCompliance javaCompliance, StringFilter testClassNameFilter,
			Charset encoding,
			Set<Class<? extends JavaModuleCharacteristic>> characteristics,
			List<String> rawCompilerArgs, TestRunner testRunner,
			ScalaVersion scalaVersion) {
		super(characteristics);
		this.name = name;
		this.generatorSourcesToFollow = generatorSourcesToFollow;
		this.codeStylePolicy = codeStylePolicy;
		this.locationUnderWsRoot = locationUnderWsRoot;
		this.mainJavas = mainJavas;
		this.mainResources = mainResources;
		this.testJavas = testJavas;
		this.testResources = testResources;
		this.testEnv = testEnv;
		this.codeFormatterPolicy = codeFormatterPolicy;
		this.javaCompliance = javaCompliance;
		this.testClassNameFilter = testClassNameFilter;
		this.encoding = encoding;
		this.rawCompilerArgs = rawCompilerArgs;
		this.testRunner = testRunner;
		this.scalaVersion = scalaVersion;
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
		for (Class<? extends JavaModuleCharacteristic> c : m
				.characteristics()) {
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
		private final List<String> mainJavas = new ArrayList<>();
		private final List<String> mainResources = new ArrayList<>();
		private final List<String> testJavas = new ArrayList<>();
		private final List<String> testResources = new ArrayList<>();
		private SystemEnv testEnv;
		private final Set<JavaModule> mainDepsForCompilation = new LinkedHashSet<>();
		private final Set<JavaModule> mainDepsForRunOnly = new LinkedHashSet<>();
		private final Set<JavaModule> testDepsForCompilationExcludingMainDeps = new LinkedHashSet<>();
		private final Set<JavaModule> testDepsForRunOnlyExcludingMainDeps = new LinkedHashSet<>();
		private Target generatedClasses;
		private Target generatedSrc;
		private final List<Source> generatorSourcesToFollow = new ArrayList<>();
		private CodeStylePolicy codeStylePolicy = CodeStylePolicy
				.defaultsExcept().end();
		private CodeFormatterPolicy codeFormatterPolicy = new CodeFormatterPolicy();
		private JavaCompliance javaCompliance = JavaCompliance.JAVA_1_8;
		private String locationUnderWsRoot;
		private StringFilter testClassNameFilter = new DefaultTestClassNameFilter();
		private Charset encoding;
		private final Set<Class<? extends JavaModuleCharacteristic>> characteristics = new HashSet<>();
		private final List<String> rawCompilerArgs = new ArrayList<>();
		private TestRunner testRunner;
		private ScalaVersion scalaVersion;

		public JavaSrcModule end() {
			if (locationUnderWsRoot != null && relativeParentDir != null) {
				throw new IllegalArgumentException(
						"You must not specify both relativeParentDir and locationUnderWsRoot");
			}
			String locationUnderWsRootToUse;
			if (locationUnderWsRoot != null) {
				locationUnderWsRootToUse = locationUnderWsRoot;
			} else {
				locationUnderWsRootToUse = normalizedRelativeParentDir(
						relativeParentDir) + name;
			}
			return new JavaSrcModule(name, locationUnderWsRootToUse, mainJavas,
					mainResources, testJavas, testResources, testEnv,
					mainDepsForCompilation, mainDepsForRunOnly,
					testDepsForCompilationExcludingMainDeps,
					testDepsForRunOnlyExcludingMainDeps, generatedClasses,
					generatedSrc, generatorSourcesToFollow, codeStylePolicy,
					codeFormatterPolicy, javaCompliance, testClassNameFilter,
					encoding, characteristics, rawCompilerArgs, testRunner,
					scalaVersion);
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

		public IwantSrcModuleSpex locationUnderWsRoot(
				String locationUnderWsRoot) {
			this.locationUnderWsRoot = locationUnderWsRoot;
			return this;
		}

		public IwantSrcModuleSpex mavenLayout() {
			return mainJava("src/main/java").mainResources("src/main/resources")
					.testJava("src/test/java")
					.testResources("src/test/resources");
		}

		public IwantSrcModuleSpex mainJava(String mainJava) {
			this.mainJavas.add(mainJava);
			return this;
		}

		/**
		 * At the moment there is no difference between calling this and
		 * mainJava, but it's recommended to declare the intent, if you use
		 * separate source directories.
		 */
		public IwantSrcModuleSpex mainScala(String mainScala) {
			return mainJava(mainScala);
		}

		public IwantSrcModuleSpex noMainJava() {
			this.mainJavas.clear();
			return this;
		}

		public IwantSrcModuleSpex mainResources(String mainResources) {
			this.mainResources.add(mainResources);
			return this;
		}

		public IwantSrcModuleSpex noMainResources() {
			this.mainResources.clear();
			return this;
		}

		public IwantSrcModuleSpex testJava(String testJava) {
			this.testJavas.add(testJava);
			return this;
		}

		public IwantSrcModuleSpex testScala(String testScala) {
			return testJava(testScala);
		}

		public IwantSrcModuleSpex noTestJava() {
			this.testJavas.clear();
			return this;
		}

		public IwantSrcModuleSpex testResources(String testResources) {
			this.testResources.add(testResources);
			return this;
		}

		public IwantSrcModuleSpex noTestResources() {
			this.testResources.clear();
			return this;
		}

		public IwantSrcModuleSpex testEnv(SystemEnv testEnv) {
			this.testEnv = testEnv;
			return this;
		}

		public IwantSrcModuleSpex mainDeps(
				JavaModule... mainDepsForCompilation) {
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
			return testDeps(
					Arrays.asList(testDepsForCompilationExcludingMainDeps));
		}

		public IwantSrcModuleSpex testDeps(
				Collection<? extends JavaModule> testDepsForCompilationExcludingMainDeps) {
			this.testDepsForCompilationExcludingMainDeps
					.addAll(testDepsForCompilationExcludingMainDeps);
			return this;
		}

		public IwantSrcModuleSpex testRuntimeDeps(
				JavaModule... testDepsForRunOnlyExcludingMainDeps) {
			return testRuntimeDeps(
					Arrays.asList(testDepsForRunOnlyExcludingMainDeps));
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

		public IwantSrcModuleSpex rawCompilerArgs(String... rawCompilerArgs) {
			return rawCompilerArgs(Arrays.asList(rawCompilerArgs));
		}

		public IwantSrcModuleSpex rawCompilerArgs(
				Collection<? extends String> rawCompilerArgs) {
			this.rawCompilerArgs.addAll(rawCompilerArgs);
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
			return generatorSourcesToFollow(
					Arrays.asList(generatorSourcesToFollow));
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

		public IwantSrcModuleSpex javaCompliance(
				JavaCompliance javaCompliance) {
			this.javaCompliance = javaCompliance;
			return this;
		}

		public IwantSrcModuleSpex testedBy(String testSuiteName) {
			this.testClassNameFilter = new StringFilterByEquality(
					testSuiteName);
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

		public IwantSrcModuleSpex testRunner(TestRunner testRunner) {
			this.testRunner = testRunner;
			return this;
		}

		public IwantSrcModuleSpex scalaVersion(ScalaVersion scalaVersion) {
			this.scalaVersion = scalaVersion;
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

	public SystemEnv testEnv() {
		return testEnv;
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

	public JavaCompliance javaCompliance() {
		return javaCompliance;
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
		List<Path> retval = new ArrayList<>();
		for (String mainJava : mainJavas) {
			retval.add(
					Source.underWsroot(locationUnderWsRoot() + "/" + mainJava));
		}
		return retval;
	}

	public List<Path> mainResourcesAsPaths() {
		List<Path> retval = new ArrayList<>();
		for (String res : mainResources) {
			retval.add(Source.underWsroot(locationUnderWsRoot() + "/" + res));
		}
		return retval;
	}

	public List<Path> testJavasAsPaths() {
		List<Path> retval = new ArrayList<>();
		for (String testJava : testJavas) {
			retval.add(
					Source.underWsroot(locationUnderWsRoot() + "/" + testJava));
		}
		return retval;
	}

	public List<Path> testResourcesAsPaths() {
		List<Path> retval = new ArrayList<>();
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
		List<Path> classpath = new ArrayList<>();
		for (JavaModule mainDep : mainDepsForCompilation()) {
			Path depArtifact = mainDep.mainArtifact();
			if (depArtifact != null) {
				classpath.add(depArtifact);
			}
		}
		Path scalaClasses = null;
		if (scalaVersion != null) {
			scalaClasses = ScalaClasses.with()
					.name(name() + "-main-classes-from-scala")
					.scala(scalaVersion).srcDirs(mainJavasAsPaths())
					.classLocations(classpath).end();
			classpath.add(0, scalaClasses);
		}
		JavaClassesSpex javaClasses = JavaClasses.with()
				.name(name() + "-main-classes").srcDirs(mainJavasAsPaths())
				.encoding(encoding).sourceVersion(javaCompliance)
				.resourceDirs(mainResourcesAsPaths()).classLocations(classpath)
				.debug(true).rawArgs(rawCompilerArgs);
		if (scalaClasses != null) {
			javaClasses.resourceDirs(scalaClasses);
		}
		return javaClasses.end();
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
		List<Path> classpath = new ArrayList<>();
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
		Path scalaClasses = null;
		if (scalaVersion != null) {
			scalaClasses = ScalaClasses.with()
					.name(name() + "-test-classes-from-scala")
					.scala(scalaVersion).srcDirs(testJavasAsPaths())
					.classLocations(classpath).end();
			classpath.add(0, scalaClasses);
		}
		JavaClassesSpex javaClasses = JavaClasses.with()
				.name(name() + "-test-classes").srcDirs(testJavasAsPaths())
				.encoding(encoding).sourceVersion(javaCompliance)
				.resourceDirs(testResourcesAsPaths()).classLocations(classpath)
				.debug(true).rawArgs(rawCompilerArgs);
		if (scalaClasses != null) {
			javaClasses.resourceDirs(scalaClasses);
		}
		return javaClasses.end();
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

	public TestRunner testRunner() {
		return testRunner;
	}

	public ScalaVersion scalaVersion() {
		return scalaVersion;
	}

}
