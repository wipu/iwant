package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry3.FileUtil;

public class EmmaCoverage extends Target {

	private final Path emma;
	private final List<Path> antJars;
	private final String mainClass;
	private final List<String> mainClassArguments;
	private final Path mainClassArgumentsFile;
	private final List<EmmaInstrumentation> instrumentations;
	private final List<Path> nonInstrumentedDeps;

	public EmmaCoverage(String name, Path emma, List<Path> antJars,
			String mainClass, List<String> mainClassArguments,
			Path mainClassArgumentsFile,
			List<EmmaInstrumentation> instrumentations,
			List<Path> nonInstrumentedDeps) {
		super(name);
		this.emma = emma;
		this.antJars = antJars;
		this.mainClass = mainClass;
		this.mainClassArguments = mainClassArguments;
		this.mainClassArgumentsFile = mainClassArgumentsFile;
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
		private Path mainClassArgumentsFile;

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

		public EmmaCoverageSpex mainClassAndArguments(String mainClass,
				Path mainClassArgumentsFile) {
			this.mainClass = mainClass;
			this.mainClassArgumentsFile = mainClassArgumentsFile;
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
					mainClassArguments, mainClassArgumentsFile,
					instrumentations, nonInstrumentedDeps);
		}

	}

	@Override
	public boolean supportsParallelism() {
		return false;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File ec = coverageFile(ctx);

		List<String> mainArgsToUse = mainArgsToUse(ctx);
		System.err.println(mainClass + " " + mainArgsToUse);

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

		for (String arg : mainArgsToUse) {
			script.append("      <arg value='").append(arg).append("' />\n");
		}

		script.append("      <classpath>\n");
		for (EmmaInstrumentation instrumentation : instrumentations) {
			script.append("        <pathelement location='")
					.append(ctx.cached(instrumentation))
					.append("/instr-classes'/>\n");
			// interfaces are only found from the original classes:
			script.append("        <pathelement location='")
					.append(ctx.cached(instrumentation.classesAndSources()
							.classes())).append("'/>\n");
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

	private List<String> mainArgsToUse(TargetEvaluationContext ctx) {
		if (mainClassArgumentsFile == null) {
			return mainClassArguments;
		} else {
			return mainArgsFromFile(ctx.cached(mainClassArgumentsFile));
		}
	}

	private static List<String> mainArgsFromFile(File argumentsFile) {
		String content = FileUtil.contentAsString(argumentsFile);
		String[] lines = content.split("\n");
		return Arrays.asList(lines);
	}

	public File coverageFile(TargetEvaluationContext ctx) {
		return new File(ctx.cached(this), "coverage.ec");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.addAll(antJars);
		ingredients.add(emma);
		ingredients.addAll(instrumentations);
		ingredients.addAll(nonInstrumentedDeps);
		if (mainClassArgumentsFile != null) {
			ingredients.add(mainClassArgumentsFile);
		}
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
