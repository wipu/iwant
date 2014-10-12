package net.sf.iwant.deprecated.emma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.core.ant.AntGenerated;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class EmmaCoverage extends Target {

	private final Path emma;
	private final List<Path> antJars;
	private final String mainClass;
	private final List<String> mainClassArguments;
	private final Path mainClassArgumentsFile;
	private final List<ClasspathItem> classpath;
	private final List<String> jvmargs;

	public EmmaCoverage(String name, Path emma, List<Path> antJars,
			String mainClass, List<String> mainClassArguments,
			Path mainClassArgumentsFile, List<ClasspathItem> classpath,
			List<String> jvmargs) {
		super(name);
		this.emma = emma;
		this.antJars = antJars;
		this.mainClass = mainClass;
		this.mainClassArguments = mainClassArguments;
		this.mainClassArgumentsFile = mainClassArgumentsFile;
		this.classpath = classpath;
		this.jvmargs = jvmargs;
	}

	public static EmmaCoverageSpex with() {
		return new EmmaCoverageSpex();
	}

	public static class EmmaCoverageSpex {

		private String name;
		private final List<Path> antJars = new ArrayList<Path>();
		private Path emma;
		private String mainClass;
		private List<String> mainClassArguments;
		private final List<ClasspathItem> classpath = new ArrayList<ClasspathItem>();
		private Path mainClassArgumentsFile;
		private final List<String> jvmargs = new ArrayList<String>();

		public EmmaCoverageSpex() {
			jvmArgs("-XX:-UseSplitVerifier", "-Demma.rt.control=false");
		}

		public EmmaCoverageSpex name(String name) {
			this.name = name;
			return this;
		}

		public EmmaCoverageSpex antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public EmmaCoverageSpex antJars(Collection<? extends Path> antJars) {
			this.antJars.addAll(antJars);
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
			for (EmmaInstrumentation instrumentation : instrumentations) {
				this.classpath.add(new InstrumentedClasspathItem(
						instrumentation));
			}
			return this;
		}

		public EmmaCoverageSpex nonInstrumentedClasses(
				Path... nonInstrumentedDeps) {
			return nonInstrumentedClasses(Arrays.asList(nonInstrumentedDeps));
		}

		public EmmaCoverageSpex nonInstrumentedClasses(
				Collection<? extends Path> nonInstrumentedDeps) {
			for (Path classes : nonInstrumentedDeps) {
				this.classpath.add(new NonInstrumentedClasspathItem(classes));
			}
			return this;
		}

		public EmmaCoverageSpex noJvmArgs() {
			jvmargs.clear();
			return this;
		}

		public EmmaCoverageSpex jvmArgs(String... jvmargs) {
			this.jvmargs.addAll(Arrays.asList(jvmargs));
			return this;
		}

		public EmmaCoverage end() {
			return new EmmaCoverage(name, emma, antJars, mainClass,
					mainClassArguments, mainClassArgumentsFile, classpath,
					jvmargs);
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

		String script = antScriptContent(ctx);
		File scriptFile = Iwant
				.newTextFile(new File(dest, "build.xml"), script);

		List<File> cachedAntJars = new ArrayList<File>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntGenerated.runAnt(cachedAntJars, scriptFile);
	}

	private String antScriptContent(TargetEvaluationContext ctx)
			throws IOException {
		File ec = coverageFile(ctx);
		List<String> mainArgsToUse = mainArgsToUse(ctx);

		StringBuilder script = new StringBuilder();
		script.append("<project name='emma-coverage' default='emma-coverage'>\n");
		script.append("  <target name='emma-coverage'>\n");
		script.append("    <echo message='Running " + mainClass + "' />\n");
		script.append("    <java\n");
		script.append("      classname='" + mainClass + "'\n");
		script.append("      fork='true'\n");
		script.append("      failonerror='true'\n");
		script.append("    >\n");

		for (String jvmarg : jvmargs) {
			script.append("      <jvmarg value=\"" + jvmarg + "\"/>\n");
		}

		script.append("      <sysproperty key='emma.coverage.out.file' value='")
				.append(ctx.iwant().pathWithoutBackslashes(ec))
				.append("' />\n");

		for (String arg : mainArgsToUse) {
			script.append("      <arg value='").append(arg).append("' />\n");
		}

		script.append("      <classpath>\n");
		for (ClasspathItem cpItem : classpath) {
			cpItem.toAnt(ctx, script);
		}
		script.append("        <pathelement location='")
				.append(ctx.iwant().pathWithoutBackslashes(ctx.cached(emma)))
				.append("'/>\n");
		script.append("      </classpath>\n");
		script.append("    </java>\n");
		script.append("  </target>\n");
		script.append("</project>\n");
		return script.toString();
	}

	private interface ClasspathItem {

		void toAnt(TargetEvaluationContext ctx, StringBuilder script)
				throws IOException;

		Path ingredient();

	}

	private static class InstrumentedClasspathItem implements ClasspathItem {

		private final EmmaInstrumentation instrumentation;

		public InstrumentedClasspathItem(EmmaInstrumentation instrumentation) {
			this.instrumentation = instrumentation;
		}

		@Override
		public Path ingredient() {
			return instrumentation;
		}

		@Override
		public void toAnt(TargetEvaluationContext ctx, StringBuilder script)
				throws IOException {
			script.append("        <pathelement location='")
					.append(ctx.iwant().pathWithoutBackslashes(
							ctx.cached(instrumentation)))
					.append("/instr-classes'/>\n");
		}

		@Override
		public String toString() {
			return "instr:" + instrumentation.classesAndSources().classes();
		}

	}

	private static class NonInstrumentedClasspathItem implements ClasspathItem {

		private final Path classes;

		public NonInstrumentedClasspathItem(Path classes) {
			this.classes = classes;
		}

		@Override
		public Path ingredient() {
			return classes;
		}

		@Override
		public void toAnt(TargetEvaluationContext ctx, StringBuilder script)
				throws IOException {
			script.append("        <pathelement location='")
					.append(ctx.iwant().pathWithoutBackslashes(
							ctx.cached(classes))).append("'/>\n");
		}

		@Override
		public String toString() {
			return "noninstr:" + classes;
		}

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
		ingredients.addAll(classPathIngredients());
		if (mainClassArgumentsFile != null) {
			ingredients.add(mainClassArgumentsFile);
		}
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("emma:").append(emma).append("\n");
		b.append("antJars:").append(antJars).append("\n");
		b.append("mainClass:").append(mainClass).append("\n");
		b.append("mainClassArguments:").append(mainClassArguments).append("\n");
		b.append("mainClassArgumentsFile:").append(mainClassArgumentsFile)
				.append("\n");
		b.append("classpath:").append(classpath).append("\n");
		b.append("jvmargs:").append(jvmargs).append("\n");
		return b.toString();
	}

	public List<Path> classPathIngredients() {
		List<Path> paths = new ArrayList<Path>();
		for (ClasspathItem cpItem : classpath) {
			paths.add(cpItem.ingredient());
		}
		return paths;
	}

	public String mainClass() {
		return mainClass;
	}

	public List<String> mainClassArguments() {
		return mainClassArguments;
	}

	public Path mainClassArgumentsFile() {
		return mainClassArgumentsFile;
	}

	public List<String> jvmargs() {
		return jvmargs;
	}

}