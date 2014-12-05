package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.core.TargetBase;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.core.ant.AntGenerated;

import org.apache.commons.io.FileUtils;

public class FindbugsReport extends TargetBase {

	private final List<JavaClassesAndSources> classesToAnalyze;
	private final List<Path> auxClasses;
	private final FindbugsDistribution findbugs;
	private final Path antJar;
	private final Path antLauncherJar;
	private final FindbugsOutputFormat outputFormat;

	public FindbugsReport(String name,
			List<JavaClassesAndSources> classesToAnalyze,
			List<Path> auxClasses, FindbugsDistribution findbugs, Path antJar,
			Path antLauncherJar, FindbugsOutputFormat outputFormat) {
		super(name);
		this.classesToAnalyze = classesToAnalyze;
		this.auxClasses = auxClasses;
		this.outputFormat = outputFormat;
		if (findbugs == null) {
			throw new IllegalArgumentException(
					"Please specify the findbugs distribution to use.");
		}
		this.findbugs = findbugs;
		this.antJar = antJar;
		this.antLauncherJar = antLauncherJar;
	}

	public static FindbugsReportSpex with() {
		return new FindbugsReportSpex();
	}

	public static class FindbugsReportSpex {

		private String name;
		private final List<JavaClassesAndSources> classesToAnalyze = new ArrayList<>();
		private final List<Path> auxClasses = new ArrayList<>();
		private FindbugsDistribution findbugs;
		private Path antJar;
		private Path antLauncherJar;
		private FindbugsOutputFormat outputFormat = FindbugsOutputFormat.HTML;

		public FindbugsReport end() {
			return new FindbugsReport(name, classesToAnalyze, auxClasses,
					findbugs, antJar, antLauncherJar, outputFormat);
		}

		public FindbugsReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public FindbugsReportSpex using(FindbugsDistribution findbugs,
				Path antJar, Path antLauncherJar) {
			this.findbugs = findbugs;
			this.antJar = antJar;
			this.antLauncherJar = antLauncherJar;
			return this;
		}

		public FindbugsReportSpex classesToAnalyze(
				JavaClassesAndSources... classLocations) {
			return classesToAnalyze(Arrays.asList(classLocations));
		}

		public FindbugsReportSpex classesToAnalyze(
				Collection<? extends JavaClassesAndSources> classLocations) {
			this.classesToAnalyze.addAll(classLocations);
			return this;
		}

		public FindbugsReportSpex modulesToAnalyze(JavaSrcModule... modules) {
			return modulesToAnalyze(Arrays.asList(modules));
		}

		public FindbugsReportSpex modulesToAnalyze(
				Collection<? extends JavaSrcModule> modules) {
			for (JavaSrcModule mod : modules) {
				if (mod.mainArtifact() != null) {
					JavaClassesAndSources main = new JavaClassesAndSources(
							mod.mainArtifact(), mod.mainJavasAsPaths());
					classesToAnalyze(main);
				}
				if (mod.testArtifact() != null) {
					JavaClassesAndSources test = new JavaClassesAndSources(
							mod.testArtifact(), mod.testJavasAsPaths());
					classesToAnalyze(test);
				}
				for (JavaModule aux : mod.effectivePathForTestRuntime()) {
					if (aux instanceof JavaBinModule) {
						auxClasses(aux.mainArtifact());
					}
				}
			}
			return this;
		}

		public FindbugsReportSpex auxClasses(Path... auxClasses) {
			return auxClasses(Arrays.asList(auxClasses));
		}

		public FindbugsReportSpex auxClasses(
				Collection<? extends Path> auxClasses) {
			this.auxClasses.addAll(auxClasses);
			return this;
		}

		public FindbugsReportSpex outputFormat(FindbugsOutputFormat outputFormat) {
			this.outputFormat = outputFormat;
			return this;
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		iUse.ingredients("findbugs", findbugs);
		iUse.ingredients("antJar", antJar);
		iUse.ingredients("antLauncherJar", antLauncherJar);
		for (JavaClassesAndSources cs : classesToAnalyze) {
			iUse.ingredients("classes", cs.classes());
			iUse.ingredients("sources", cs.sources());
		}

		iUse.ingredients("auxClasses", auxClasses);
		iUse.parameter("output-format", outputFormat);
		return iUse.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File buildXml = new File(dest, "build.xml");
		FileUtils.writeStringToFile(buildXml, antScript(ctx));

		List<File> cachedAnts = new ArrayList<>();
		cachedAnts.add(ctx.cached(antJar));
		cachedAnts.add(ctx.cached(antLauncherJar));

		AntGenerated.runAnt(cachedAnts, buildXml);
	}

	private String antScript(TargetEvaluationContext ctx) {
		String findbugsHomeString = ctx.iwant().pathWithoutBackslashes(
				findbugs.homeDirectory(ctx));
		String destString = ctx.iwant()
				.pathWithoutBackslashes(ctx.cached(this));

		StringBuilder xml = new StringBuilder();
		xml.append("<project name=\"findbugs\" basedir=\".\" default=\"findbugs-report\">\n");
		xml.append("\n");
		xml.append("    <!-- This is a generated file, don't edit manually. -->\n");
		xml.append("\n");
		xml.append("    <target name=\"findbugs-home\">\n");
		xml.append("        <property name=\"findbugs-home\" location=\""
				+ findbugsHomeString + "\" />\n");
		xml.append("    </target>\n");
		xml.append("\n");
		xml.append("    <target name=\"findbugs-task-classpath\" depends=\"findbugs-home\">\n");
		xml.append("        <path id=\"findbugs-task-classpath\">\n");
		xml.append("            <fileset dir=\"${findbugs-home}/lib\" includes=\"*.jar\" />\n");
		xml.append("        </path>\n");
		xml.append("    </target>\n");
		xml.append("\n");
		xml.append("    <target name=\"findbugs-report\" description=\"findbugs report of all code\" depends=\"findbugs-task-classpath, findbugs-home\">\n");
		xml.append("        <taskdef name=\"findbugs\" classname=\"edu.umd.cs.findbugs.anttask.FindBugsTask\" classpathref=\"findbugs-task-classpath\" />\n");
		xml.append("        <property name=\"findbugs-report\" location=\""
				+ destString + "/findbugs-report\" />\n");
		xml.append("        <delete dir=\"${findbugs-report}\" />\n");
		xml.append("        <mkdir dir=\"${findbugs-report}\" />\n");
		xml.append("        <findbugs home=\"${findbugs-home}\" output=\""
				+ outputFormat + "\" outputfile=\"${findbugs-report}/" + name()
				+ "." + outputFormat + "\">\n");
		xml.append("\n");

		for (JavaClassesAndSources cs : classesToAnalyze) {
			xml.append("            <class location=\"")
					.append(ctx.iwant().pathWithoutBackslashes(
							ctx.cached(cs.classes()))).append("\" />\n");
			for (Path src : cs.sources()) {
				xml.append("            <sourcepath path=\"")
						.append(ctx.iwant().pathWithoutBackslashes(
								ctx.cached(src))).append("\" />\n");
			}
		}
		for (Path aux : auxClasses) {
			xml.append("            <auxclasspath path=\"")
					.append(ctx.iwant().pathWithoutBackslashes(ctx.cached(aux)))
					.append("\" />\n");
		}

		xml.append("       </findbugs>\n");
		xml.append("    </target>\n");
		xml.append("\n");
		xml.append("</project>\n");
		return xml.toString();
	}

	public List<JavaClassesAndSources> classesToAnalyze() {
		return classesToAnalyze;
	}

	public List<Path> auxClasses() {
		return auxClasses;
	}

}
