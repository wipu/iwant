package net.sf.iwant.api.javamodules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.antrunner.AntRunner;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.coreservices.FileUtil;

public class ScalaClasses extends TargetBase {

	private final List<Path> srcDirs;
	private final List<Path> classLocations;
	private final ScalaVersion scala;
	private final Path antJar = TestedIwantDependencies.antJar();
	private final Path antLauncherJar = TestedIwantDependencies
			.antLauncherJar();

	private ScalaClasses(String name, List<Path> srcDirs,
			List<Path> classLocations, ScalaVersion scala) {
		super(name);
		this.srcDirs = srcDirs;
		this.classLocations = classLocations;
		this.scala = scala;
	}

	public static ScalaClassesSpex with() {
		return new ScalaClassesSpex();
	}

	public static class ScalaClassesSpex {

		private String name;
		private final List<Path> srcDirs = new ArrayList<>();
		private final List<Path> classLocations = new ArrayList<>();
		private ScalaVersion scala;

		public ScalaClasses end() {
			return new ScalaClasses(name, srcDirs, classLocations, scala);
		}

		public ScalaClassesSpex name(String name) {
			this.name = name;
			return this;
		}

		public ScalaClassesSpex srcDirs(Path... srcDirs) {
			return srcDirs(Arrays.asList(srcDirs));
		}

		public ScalaClassesSpex srcDirs(Collection<? extends Path> srcDirs) {
			this.srcDirs.addAll(srcDirs);
			return this;
		}

		public ScalaClassesSpex classLocations(Path... classLocations) {
			return classLocations(Arrays.asList(classLocations));
		}

		public ScalaClassesSpex classLocations(
				Collection<? extends Path> classLocations) {
			this.classLocations.addAll(classLocations);
			return this;
		}

		public ScalaClassesSpex scala(ScalaVersion scala) {
			this.scala = scala;
			return this;
		}

	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("srcDirs", srcDirs)
				.ingredients("classLocations", classLocations)
				.ingredients("scala-compiler", scala.compilerJar())
				.ingredients("scala-library", scala.libraryJar())
				.ingredients("scala-reflect", scala.reflectJar())
				.ingredients("antJar", antJar)
				.ingredients("antLauncherJar", antLauncherJar).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File tmp = ctx.freshTemporaryDirectory();
		File buildXml = new File(tmp, "build.xml");
		FileUtil.newTextFile(buildXml, antScript(ctx));

		List<File> antJars = Arrays.asList(ctx.cached(antJar),
				ctx.cached(antLauncherJar));

		AntRunner.runAnt(antJars, buildXml);
	}

	public List<Path> srcDirs() {
		return srcDirs;
	}

	public List<Path> classLocations() {
		return classLocations;
	}

	public ScalaVersion scala() {
		return scala;
	}

	private String antScript(TargetEvaluationContext ctx) {
		StringBuilder ant = new StringBuilder();
		ant.append("<project name=\"" + name()
				+ "-scalac\" default=\"classes-from-scala\" basedir=\".\">\n");
		ant.append("\n");
		ant.append("	<property name=\"scala-compiler.jar\" location=\""
				+ ctx.cached(scala.compilerJar()) + "\" />\n");
		ant.append("	<property name=\"scala-library.jar\" location=\""
				+ ctx.cached(scala.libraryJar()) + "\" />\n");
		ant.append("	<property name=\"scala-reflect.jar\" location=\""
				+ ctx.cached(scala.reflectJar()) + "\" />\n");
		ant.append("\n");
		ant.append("	<target name=\"scalac-taskdef\">\n");
		ant.append(
				"		<taskdef resource=\"scala/tools/ant/antlib.xml\">\n");
		ant.append("			<classpath>\n");
		ant.append(
				"				<pathelement location=\"${scala-compiler.jar}\" />\n");
		ant.append(
				"				<pathelement location=\"${scala-library.jar}\" />\n");
		ant.append(
				"				<pathelement location=\"${scala-reflect.jar}\" />\n");
		ant.append("			</classpath>\n");
		ant.append("		</taskdef>\n");
		ant.append("	</target>\n");
		ant.append("\n");
		ant.append("	<target name=\"scalac-classpath\">\n");
		ant.append("		<path id=\"scalac-classpath\">\n");
		for (Path dep : classLocations) {
			ant.append("			<pathelement location=\"" + ctx.cached(dep)
					+ "\" />\n");
		}
		ant.append(
				"			<pathelement location=\"${scala-library.jar}\" />\n");
		ant.append("		</path>\n");
		ant.append("	</target>\n");
		ant.append("\n");
		ant.append(
				"	<target name=\"classes-from-scala\" depends=\"scalac-taskdef, scalac-classpath\">\n");
		ant.append("		<scalac destdir=\"" + ctx.cached(this)
				+ "\" classpathref=\"scalac-classpath\">\n");
		ant.append("			<src>\n");
		for (Path srcDir : srcDirs) {
			ant.append("				<pathelement location=\""
					+ ctx.cached(srcDir) + "\" />\n");
		}
		ant.append("			</src>\n");
		ant.append("			<include name=\"**/*.java\" />\n");
		ant.append("			<include name=\"**/*.scala\" />\n");
		ant.append("		</scalac>\n");
		ant.append("	</target>\n");
		ant.append("\n");
		ant.append("</project>\n");
		return ant.toString();
	}

}
