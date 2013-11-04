package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.AntGenerated;
import net.sf.iwant.api.BackslashFixer;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.commons.io.FileUtils;

public class FindbugsReport extends Target {

	private final List<JavaClassesAndSources> classesToAnalyze;
	private final FindbugsDistribution findbugs;
	private final Path antJar;
	private final Path antLauncherJar;

	public FindbugsReport(String name,
			List<JavaClassesAndSources> classesToAnalyze,
			FindbugsDistribution findbugs, Path antJar, Path antLauncherJar) {
		super(name);
		this.classesToAnalyze = classesToAnalyze;
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
		private final List<JavaClassesAndSources> classesToAnalyze = new ArrayList<JavaClassesAndSources>();
		private FindbugsDistribution findbugs;
		private Path antJar;
		private Path antLauncherJar;

		public FindbugsReport end() {
			return new FindbugsReport(name, classesToAnalyze, findbugs, antJar,
					antLauncherJar);
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

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(findbugs);
		ingredients.add(antJar);
		ingredients.add(antLauncherJar);
		for (JavaClassesAndSources cs : classesToAnalyze) {
			ingredients.add(cs.classes());
			ingredients.addAll(cs.sources());
		}
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");

		b.append("  ingredients: {\n");
		for (Path ingredient : ingredients()) {
			b.append("    ").append(ingredient).append("\n");
		}
		b.append("  }\n");

		b.append("  classesToAnalyze: {\n");
		for (JavaClassesAndSources cs : classesToAnalyze) {
			b.append("    ").append(cs).append("\n");
		}
		b.append("  }\n");

		b.append("}\n");
		return b.toString();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File buildXml = new File(dest, "build.xml");
		FileUtils.writeStringToFile(buildXml, antScript(ctx));

		List<File> cachedAnts = new ArrayList<File>();
		cachedAnts.add(ctx.cached(antJar));
		cachedAnts.add(ctx.cached(antLauncherJar));

		AntGenerated.runAnt(cachedAnts, buildXml);
	}

	private String antScript(TargetEvaluationContext ctx) throws IOException {
		String findbugsHomeString = wintoySafeCanonicalPath(findbugs
				.homeDirectory(ctx));
		String destString = wintoySafeCanonicalPath(ctx.cached(this));

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
		xml.append("        <findbugs home=\"${findbugs-home}\" output=\"html\" outputfile=\"${findbugs-report}/"
				+ name() + ".html\">\n");
		xml.append("\n");

		for (JavaClassesAndSources cs : classesToAnalyze) {
			xml.append("            <class location=\"")
					.append(wintoySafeCanonicalPath(ctx.cached(cs.classes())))
					.append("\" />\n");
			for (Path src : cs.sources()) {
				xml.append("            <sourcepath path=\"")
						.append(wintoySafeCanonicalPath(ctx.cached(src)))
						.append("\" />\n");
			}
		}
		// for (Path aux : auxClasses) {
		// xml.append("            <auxclasspath path=\"")
		// .append(wintoySafeCanonicalPath(ctx.cached(aux)))
		// .append("\" />\n");
		// }

		xml.append("       </findbugs>\n");
		xml.append("    </target>\n");
		xml.append("\n");
		xml.append("</project>\n");
		return xml.toString();
	}

	private static String wintoySafeCanonicalPath(File file) throws IOException {
		return BackslashFixer.wintoySafeCanonicalPath(file);
	}

}
