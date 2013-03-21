package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.entry.Iwant;

public class EmmaCoverage extends Target {

	private final Path emma;
	private final List<Path> antJars;
	private final String mainClass;
	private final List<String> mainClassArguments;
	private final List<EmmaInstrumentation> instrumentations;
	private final List<Path> nonInstrumentedDeps;

	public EmmaCoverage(String name, Path emma, List<Path> antJars,
			String mainClass, List<String> mainClassArguments,
			List<EmmaInstrumentation> instrumentations,
			List<Path> nonInstrumentedDeps) {
		super(name);
		this.emma = emma;
		this.antJars = antJars;
		this.mainClass = mainClass;
		this.mainClassArguments = mainClassArguments;
		this.instrumentations = instrumentations;
		this.nonInstrumentedDeps = nonInstrumentedDeps;
	}

	public static EmmaCoverageSpex with() {
		return new EmmaCoverageSpex();
	}

	public static class EmmaCoverageSpex {

		private String name;
		private List<Path> antJars;
		private Path emma;
		private String mainClass;
		private List<String> mainClassArguments;
		private final List<EmmaInstrumentation> instrumentations = new ArrayList<EmmaInstrumentation>();
		private final List<Path> nonInstrumentedDeps = new ArrayList<Path>();

		public EmmaCoverageSpex name(String name) {
			this.name = name;
			return this;
		}

		public EmmaCoverageSpex antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public EmmaCoverageSpex antJars(List<Path> antJars) {
			this.antJars = antJars;
			return this;
		}

		public EmmaCoverageSpex emma(Path emma) {
			this.emma = emma;
			return this;
		}

		public EmmaCoverageSpex mainClassAndArguments(String mainClass,
				String... mainClassArguments) {
			this.mainClass = mainClass;
			this.mainClassArguments = Arrays.asList(mainClassArguments);
			return this;
		}

		public EmmaCoverageSpex instrumentations(
				EmmaInstrumentation... instrumentations) {
			this.instrumentations.addAll(Arrays.asList(instrumentations));
			return this;
		}

		public EmmaCoverageSpex nonInstrumentedClasses(
				Path... nonInstrumentedDeps) {
			return nonInstrumentedClasses(Arrays.asList(nonInstrumentedDeps));
		}

		public EmmaCoverageSpex nonInstrumentedClasses(
				Collection<? extends Path> nonInstrumentedDeps) {
			this.nonInstrumentedDeps.addAll(nonInstrumentedDeps);
			return this;
		}

		public EmmaCoverage end() {
			return new EmmaCoverage(name, emma, antJars, mainClass,
					mainClassArguments, instrumentations, nonInstrumentedDeps);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File ec = new File(dest, "coverage.ec");

		StringBuilder script = new StringBuilder();
		script.append("<project name='emma-coverage' default='emma-coverage'>\n");
		script.append("  <target name='emma-coverage'>\n");
		script.append("    <echo message='Running " + mainClass + "' />\n");
		script.append("    <java\n");
		script.append("      classname='" + mainClass + "'\n");
		script.append("      fork='true'\n");
		script.append("      failonerror='true'\n");
		script.append("    >\n");

		script.append("      <sysproperty key='emma.coverage.out.file' value='")
				.append(ec.getCanonicalPath()).append("' />\n");

		for (String arg : mainClassArguments) {
			script.append("      <arg value='").append(arg).append("' />\n");
		}

		script.append("      <classpath>\n");
		for (EmmaInstrumentation instrumentation : instrumentations) {
			script.append("        <pathelement location='")
					.append(ctx.cached(instrumentation))
					.append("/instr-classes'/>\n");
		}
		for (Path nonInstrumentedDep : nonInstrumentedDeps) {
			script.append("        <pathelement location='")
					.append(ctx.cached(nonInstrumentedDep)).append("'/>\n");
		}
		script.append("        <pathelement location='")
				.append(ctx.cached(emma)).append("'/>\n");
		script.append("      </classpath>\n");
		script.append("    </java>\n");
		script.append("  </target>\n");
		script.append("</project>\n");

		File scriptFile = Iwant.newTextFile(new File(dest, "build.xml"),
				script.toString());

		List<File> cachedAntJars = new ArrayList<File>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntGenerated.runAnt(cachedAntJars, scriptFile);
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.addAll(antJars);
		ingredients.add(emma);
		ingredients.addAll(instrumentations);
		ingredients.addAll(nonInstrumentedDeps);
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
