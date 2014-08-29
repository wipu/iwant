package net.sf.iwant.api;

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

import net.sf.iwant.api.EmmaCoverage.EmmaCoverageSpex;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.StringFilter;
import net.sf.iwant.api.model.StringFilterByEquality;

public class EmmaTargetsOfJavaModules {

	private final Path emma;
	private final List<Path> antJars;
	private final SortedSet<JavaModule> modules;
	private final Path filter;
	private final Map<String, EmmaInstrumentation> instrsByName = new HashMap<String, EmmaInstrumentation>();
	private final Map<String, EmmaCoverage> coveragesByName = new HashMap<String, EmmaCoverage>();
	private final SortedSet<String> modulesNotToInstrument = new TreeSet<String>();

	private EmmaTargetsOfJavaModules(Path emma, List<Path> antJars,
			Path filter, SortedSet<JavaModule> modules,
			SortedSet<JavaModule> modulesNotToInstrument) {
		this.emma = emma;
		this.antJars = antJars;
		this.filter = filter;
		this.modules = modules;
		for (JavaModule mod : modulesNotToInstrument) {
			this.modulesNotToInstrument.add(mod.name());
		}

	}

	public static EmmaTargetsOfJavaModulesSpex with() {
		return new EmmaTargetsOfJavaModulesSpex();
	}

	public static class EmmaTargetsOfJavaModulesSpex {

		private final SortedSet<JavaModule> modules = new TreeSet<JavaModule>();
		private final SortedSet<JavaModule> modulesNotToInstrument = new TreeSet<JavaModule>();
		private Path emma;
		private final List<Path> antJars = new ArrayList<Path>();
		private Path filter;

		public EmmaTargetsOfJavaModulesSpex emma(Path emma) {
			this.emma = emma;
			return this;
		}

		public EmmaTargetsOfJavaModulesSpex antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public EmmaTargetsOfJavaModulesSpex antJars(
				Collection<? extends Path> antJars) {
			this.antJars.addAll(antJars);
			return this;
		}

		public EmmaTargetsOfJavaModulesSpex filter(Path filter) {
			this.filter = filter;
			return this;
		}

		public EmmaTargetsOfJavaModulesSpex modules(JavaModule... modules) {
			return modules(Arrays.asList(modules));
		}

		public EmmaTargetsOfJavaModulesSpex modules(
				Collection<? extends JavaModule> modules) {
			this.modules.addAll(modules);
			return this;
		}

		public EmmaTargetsOfJavaModulesSpex butNotInstrumenting(
				JavaModule... modulesNotToInstrument) {
			return butNotInstrumenting(Arrays.asList(modulesNotToInstrument));
		}

		public EmmaTargetsOfJavaModulesSpex butNotInstrumenting(
				Collection<? extends JavaModule> modulesNotToInstrument) {
			this.modulesNotToInstrument.addAll(modulesNotToInstrument);
			return this;
		}

		public EmmaTargetsOfJavaModules end() {
			return new EmmaTargetsOfJavaModules(emma, antJars, filter, modules,
					modulesNotToInstrument);
		}

	}

	public EmmaInstrumentation emmaInstrumentationOf(JavaModule mod) {
		if (modulesNotToInstrument.contains(mod.name())) {
			return null;
		}
		EmmaInstrumentation instr = instrsByName.get(mod.name());
		if (instr == null) {
			instr = EmmaInstrumentation.of(mod).filter(filter).using(emma);
			instrsByName.put(mod.name(), instr);
		}
		return instr;
	}

	public EmmaCoverage emmaCoverageOf(JavaModule mod) {
		EmmaCoverage coverage = coveragesByName.get(mod.name());
		if (coverage == null) {
			coverage = newEmmaCoverageOf(mod);
			coveragesByName.put(mod.name(), coverage);
		}
		return coverage;
	}

	private EmmaCoverage newEmmaCoverageOf(JavaModule mod) {
		if (!(mod instanceof JavaSrcModule)) {
			// only source modules have tests
			return null;
		}
		return emmaCoverageOf((JavaSrcModule) mod);
	}

	private EmmaCoverage emmaCoverageOf(JavaSrcModule mod) {
		if (mod.testArtifact() == null) {
			return null;
		}
		EmmaCoverageSpex coverage = EmmaCoverage.with()
				.name(mod.name() + ".emmacoverage").emma(emma).antJars(antJars);
		String mainClass = "org.junit.runner.JUnitCore";

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

		coverage.nonInstrumentedClasses(mod.testArtifact());
		for (JavaModule dep : mod.effectivePathForTestRuntime()) {
			dep(coverage, dep);
		}

		return coverage.end();
	}

	private void dep(EmmaCoverageSpex coverage, JavaModule mod) {
		EmmaInstrumentation instr = emmaInstrumentationOf(mod);
		if (instr != null) {
			coverage.instrumentations(instr);
		} else if (mod.mainArtifact() != null) {
			coverage.nonInstrumentedClasses(mod.mainArtifact());
		}
	}

	public EmmaReport emmaReport(String name) {
		Set<EmmaInstrumentation> instrs = new LinkedHashSet<EmmaInstrumentation>();
		Set<EmmaCoverage> coverages = new LinkedHashSet<EmmaCoverage>();
		for (JavaModule mod : modules) {
			EmmaInstrumentation instr = emmaInstrumentationOf(mod);
			if (instr != null) {
				instrs.add(instr);
			}
			EmmaCoverage coverage = emmaCoverageOf(mod);
			if (coverage != null) {
				coverages.add(coverage);
			}
		}
		return EmmaReport.with().name(name).emma(emma).instrumentations(instrs)
				.coverages(coverages).end();
	}

}
