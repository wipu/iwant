package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.AntGenerated;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.commons.io.FileUtils;

public class JacocoCoverage extends Target {

	private final List<Path> classLocations;
	private final List<Path> antJars;
	private final JacocoDistribution jacoco;
	private final Collection<? extends Path> deps;
	private final String mainClassName;
	private final List<String> mainClassArgs;
	private final Path mainClassArgsFile;

	public JacocoCoverage(String name, List<Path> classLocations,
			List<Path> antJars, JacocoDistribution jacoco,
			Collection<? extends Path> deps, String mainClassName,
			List<String> mainClassArgs, Path mainClassArgsFile) {
		super(name);
		this.classLocations = classLocations;
		this.antJars = antJars;
		this.jacoco = jacoco;
		this.deps = deps;
		this.mainClassName = mainClassName;
		this.mainClassArgs = mainClassArgs;
		this.mainClassArgsFile = mainClassArgsFile;
	}

	public static JacocoCoverageSpexPlease with() {
		return new JacocoCoverageSpexPlease();
	}

	public static class JacocoCoverageSpexPlease {

		private String name;
		private final List<Path> classLocations = new ArrayList<Path>();
		private final List<Path> antJars = new ArrayList<Path>();
		private JacocoDistribution jacoco;
		private String mainClassName;
		private List<String> mainClassArgs;
		private Path mainClassArgsFile;
		private Collection<? extends Path> deps;

		public JacocoCoverageSpexPlease name(String name) {
			this.name = name;
			return this;
		}

		public JacocoCoverage end() {
			return new JacocoCoverage(name, classLocations, antJars, jacoco,
					deps, mainClassName, mainClassArgs, mainClassArgsFile);
		}

		public JacocoCoverageSpexPlease classLocations(Path... classLocations) {
			return classLocations(Arrays.asList(classLocations));
		}

		public JacocoCoverageSpexPlease classLocations(
				Collection<? extends Path> classLocations) {
			this.classLocations.addAll(classLocations);
			return this;
		}

		public JacocoCoverageSpexPlease antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public JacocoCoverageSpexPlease antJars(
				Collection<? extends Path> antJars) {
			this.antJars.addAll(antJars);
			return this;
		}

		public JacocoCoverageSpexPlease jacocoWithDeps(
				JacocoDistribution jacoco, Path... deps) {
			return jacocoWithDeps(jacoco, Arrays.asList(deps));
		}

		public JacocoCoverageSpexPlease jacocoWithDeps(
				JacocoDistribution jacoco, Collection<? extends Path> deps) {
			this.jacoco = jacoco;
			this.deps = deps;
			return this;
		}

		public JacocoCoverageSpexPlease mainClassAndArguments(
				String mainClassName, String... mainClassArgs) {
			this.mainClassName = mainClassName;
			this.mainClassArgs = Arrays.asList(mainClassArgs);
			return this;
		}

		public JacocoCoverageSpexPlease mainClassAndArguments(String mainClass,
				Path mainClassArgumentsFile) {
			this.mainClassName = mainClass;
			this.mainClassArgsFile = mainClassArgumentsFile;
			return this;
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
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.addAll(classLocations);
		ingredients.addAll(antJars);
		ingredients.add(jacoco);
		ingredients.addAll(deps);
		if (mainClassArgsFile != null) {
			ingredients.add(mainClassArgsFile);
		}
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append("\n");
		b.append("jacoco:").append(jacoco).append("\n");
		b.append("deps:").append(deps).append("\n");
		b.append("antJars:").append(antJars).append("\n");
		b.append("classLocations:").append(classLocations).append("\n");
		b.append("mainClassName:").append(mainClassName).append("\n");
		b.append("mainClassArgs:").append(mainClassArgs).append("\n");
		b.append("mainClassArgsFile:").append(mainClassArgsFile).append("\n");
		return b.toString();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File tmp = ctx.freshTemporaryDirectory();

		File antScript = new File(tmp, name() + ".xml");
		FileUtils.writeStringToFile(antScript, antScript(ctx), "UTF-8");

		List<File> cachedAntJars = new ArrayList<File>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntGenerated.runAnt(cachedAntJars, antScript);
	}

	private String antScript(TargetEvaluationContext ctx) throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("\n");
		b.append("<project name=\"" + name() + "\" default=\"" + name()
				+ "\" xmlns:jacoco=\"antlib:org.jacoco.ant\" basedir=\".\">\n");
		b.append("\n");
		b.append("      <taskdef uri=\"antlib:org.jacoco.ant\" resource=\"org/jacoco/ant/antlib.xml\">\n");
		for (Path dep : deps) {
			b.append("              <classpath location=\"" + ctx.cached(dep)
					+ "\" />\n");
		}
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoAntJar(ctx) + "\" />\n");
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoCoreJar(ctx) + "\" />\n");
		b.append("              <classpath location=\""
				+ jacoco.orgJacocoReportJar(ctx) + "\" />\n");
		b.append("      </taskdef>\n");
		b.append("\n");

		b.append("	<target name=\"" + name() + "\">\n");
		b.append("		<java classname=\"" + mainClassName
				+ "\" fork=\"true\" failonerror=\"true\">\n");
		b.append("			<classpath>\n");
		for (Path classLocation : classLocations) {
			b.append("				<pathelement location=\"" + ctx.cached(classLocation)
					+ "\" />\n");
		}
		b.append("				<pathelement location=\"" + jacoco.jacocoagentJar(ctx)
				+ "\" />\n");
		b.append("			</classpath>\n");
		b.append("			<sysproperty key=\"jacoco-agent.destfile\" file=\""
				+ ctx.cached(this) + "\" />\n");
		for (String arg : mainArgsToUse(ctx)) {
			b.append("			<arg value=\"" + arg + "\" />\n");
		}
		b.append("		</java>\n");
		b.append("	</target>\n");

		b.append("\n");
		b.append("</project>\n");
		return b.toString();
	}

	private List<String> mainArgsToUse(TargetEvaluationContext ctx)
			throws IOException {
		if (mainClassArgsFile == null) {
			return mainClassArgs;
		} else {
			return mainArgsFromFile(ctx.cached(mainClassArgsFile));
		}
	}

	private static List<String> mainArgsFromFile(File argumentsFile)
			throws IOException {
		List<String> lines = new ArrayList<String>();
		for (Object line : FileUtils.readLines(argumentsFile)) {
			lines.add((String) line);
		}
		return lines;
	}

	public String mainClassName() {
		return mainClassName;
	}

	public List<String> mainClassArgs() {
		return mainClassArgs;
	}

	public Path mainClassArgsFile() {
		return mainClassArgsFile;
	}

	public List<Path> classLocations() {
		return classLocations;
	}

}
