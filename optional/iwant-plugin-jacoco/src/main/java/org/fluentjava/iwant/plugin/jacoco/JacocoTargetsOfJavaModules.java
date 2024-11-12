package org.fluentjava.iwant.plugin.jacoco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.core.ClassNameList;
import org.fluentjava.iwant.api.core.StringFilterByEquality;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.plugin.jacoco.JacocoCoverage.JacocoCoverageSpexPlease;

public class JacocoTargetsOfJavaModules {

	private final JacocoDistribution jacoco;
	private final List<Path> antJars;
	private final Collection<? extends JavaModule> modules;
	private final Map<String, JacocoInstrumentation> instrsByName = new HashMap<>();
	private final Map<String, JacocoCoverage> coveragesByName = new HashMap<>();

	public JacocoTargetsOfJavaModules(JacocoDistribution jacoco,
			List<Path> antJars, Collection<? extends JavaModule> modules) {
		this.jacoco = jacoco;
		this.antJars = antJars;
		this.modules = modules;
	}

	public static JacocoTargetsOfJavaModulesSpexPlease with() {
		return new JacocoTargetsOfJavaModulesSpexPlease();
	}

	public static class JacocoTargetsOfJavaModulesSpexPlease {

		private JacocoDistribution jacoco;
		private final List<Path> antJars = new ArrayList<>();
		private final SortedSet<JavaModule> modules = new TreeSet<>();

		public JacocoTargetsOfJavaModules end() {
			return new JacocoTargetsOfJavaModules(jacoco, antJars, modules);
		}

		public JacocoTargetsOfJavaModulesSpexPlease jacoco(
				JacocoDistribution jacoco) {
			this.jacoco = jacoco;
			return this;
		}

		public JacocoTargetsOfJavaModulesSpexPlease antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public JacocoTargetsOfJavaModulesSpexPlease antJars(
				Collection<? extends Path> antJars) {
			this.antJars.addAll(antJars);
			return this;
		}

		public JacocoTargetsOfJavaModulesSpexPlease modules(
				JavaModule... modules) {
			return modules(Arrays.asList(modules));
		}

		public JacocoTargetsOfJavaModulesSpexPlease modules(
				Collection<? extends JavaModule> modules) {
			this.modules.addAll(modules);
			return this;
		}

	}

	public JacocoInstrumentation jacocoInstrumentationOf(JavaModule mod) {
		if (!(mod instanceof JavaSrcModule)) {
			return null;
		}
		JacocoInstrumentation instr = instrsByName.get(mod.name());
		if (instr == null) {
			Path mainArtifact = mod.mainArtifact();
			if (mainArtifact == null) {
				return null;
			}
			instr = JacocoInstrumentation.of(mainArtifact).using(jacoco,
					antJars);
			instrsByName.put(mod.name(), instr);
		}
		return instr;
	}

	public JacocoCoverage jacocoCoverageOf(JavaModule mod) {
		if (!(mod instanceof JavaSrcModule)) {
			return null;
		}
		JacocoCoverage coverage = coveragesByName.get(mod.name());
		if (coverage == null) {
			coverage = newJacocoCoverageOf((JavaSrcModule) mod);
			coveragesByName.put(mod.name(), coverage);
		}
		return coverage;
	}

	private JacocoCoverage newJacocoCoverageOf(JavaSrcModule mod) {
		if (mod.testArtifact() == null) {
			return null;
		}
		JacocoCoverageSpexPlease coverage = JacocoCoverage.with()
				.name(mod.name() + ".jacococoverage").jacoco(jacoco)
				.antJars(antJars).env(mod.testEnv());
		String mainClass = testRunnerClassNameFor(mod);

		StringFilter classNameDef = mod.testClassNameDefinition();
		if (classNameDef instanceof StringFilterByEquality) {
			StringFilterByEquality suiteNameDef = (StringFilterByEquality) classNameDef;
			coverage.mainClassAndArguments(mainClass, suiteNameDef.value());
		} else {
			ClassNameList testClassList = ClassNameList.with()
					.name(mod.name() + "-test-class-names")
					.classes(mod.testArtifact())
					.matching(mod.testClassNameDefinition()).end();
			coverage.mainClassAndArguments(mainClass, testClassList);
		}

		coverage.classLocations(mod.testArtifact());
		for (JavaModule dep : mod.effectivePathForTestRuntime()) {
			dep(coverage, dep);
		}

		return coverage.end();
	}

	private static String testRunnerClassNameFor(JavaSrcModule mod) {
		if (mod.testRunner() != null) {
			return mod.testRunner().mainClassName();
		}
		return "org.fluentjava.iwant.plugin.jacoco.Junit5Runner";
	}

	private void dep(JacocoCoverageSpexPlease coverage, JavaModule mod) {
		JacocoInstrumentation instr = jacocoInstrumentationOf(mod);
		if (instr != null) {
			coverage.classLocations(instr);
		} else if (mod.mainArtifact() != null) {
			coverage.classLocations(mod.mainArtifact());
		}
	}

	public JacocoReport jacocoReport(String name) {
		Set<JacocoCoverage> coverages = new LinkedHashSet<>();
		Set<Path> classes = new LinkedHashSet<>();
		Set<Path> sources = new LinkedHashSet<>();
		for (JavaModule mod : modules) {
			JacocoCoverage coverage = jacocoCoverageOf(mod);
			if (coverage != null) {
				coverages.add(coverage);
			}
			Path modClasses = mod.mainArtifact();
			if (modClasses != null) {
				classes.add(modClasses);
			}
			if (mod instanceof JavaSrcModule) {
				JavaSrcModule srcMod = (JavaSrcModule) mod;
				List<Path> modMainJavas = srcMod.mainJavasAsPaths();
				sources.addAll(modMainJavas);
			}
		}
		return JacocoReport.with().name(name).jacoco(jacoco).antJars(antJars)
				.coverages(coverages).classes(classes).sources(sources).end();
	}

}
