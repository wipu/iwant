package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.core.ant.AntGenerated;

import org.apache.commons.io.FileUtils;

public class JacocoReport extends Target {

	private final JacocoDistribution jacoco;
	private final List<Path> deps;
	private final List<Path> antJars;
	private final Collection<? extends JacocoCoverage> coverages;
	private final Collection<? extends Path> classes;
	private final Collection<? extends Path> sources;

	public JacocoReport(String name, JacocoDistribution jacoco,
			List<Path> deps, List<Path> antJars,
			Collection<? extends JacocoCoverage> coverages,
			Collection<? extends Path> classes,
			Collection<? extends Path> sources) {
		super(name);
		this.jacoco = jacoco;
		this.deps = deps;
		this.antJars = antJars;
		this.coverages = coverages;
		this.classes = classes;
		this.sources = sources;
	}

	public static JacocoReportSpexPlease with() {
		return new JacocoReportSpexPlease();
	}

	public static class JacocoReportSpexPlease {

		private String name;
		private JacocoDistribution jacoco;
		private List<Path> deps;
		private final List<Path> antJars = new ArrayList<Path>();
		private Collection<? extends JacocoCoverage> coverages;
		private final Collection<Path> classes = new ArrayList<Path>();
		private final Collection<Path> sources = new ArrayList<Path>();

		public JacocoReport end() {
			return new JacocoReport(name, jacoco, deps, antJars, coverages,
					classes, sources);
		}

		public JacocoReportSpexPlease name(String name) {
			this.name = name;
			return this;
		}

		public JacocoReportSpexPlease jacocoWithDeps(JacocoDistribution jacoco,
				Path... deps) {
			return jacocoWithDeps(jacoco, Arrays.asList(deps));
		}

		public JacocoReportSpexPlease jacocoWithDeps(JacocoDistribution jacoco,
				Collection<? extends Path> deps) {
			this.jacoco = jacoco;
			this.deps = new ArrayList<Path>(deps);
			return this;
		}

		public JacocoReportSpexPlease antJars(Path... antJars) {
			return antJars(Arrays.asList(antJars));
		}

		public JacocoReportSpexPlease antJars(Collection<? extends Path> antJars) {
			this.antJars.addAll(antJars);
			return this;
		}

		public JacocoReportSpexPlease coverages(JacocoCoverage... coverages) {
			return coverages(Arrays.asList(coverages));
		}

		public JacocoReportSpexPlease coverages(
				Collection<? extends JacocoCoverage> coverages) {
			this.coverages = coverages;
			return this;
		}

		public JacocoReportSpexPlease classes(Path... classes) {
			return classes(Arrays.asList(classes));
		}

		public JacocoReportSpexPlease classes(Collection<? extends Path> classes) {
			this.classes.addAll(classes);
			return this;
		}

		public JacocoReportSpexPlease sources(Path... sources) {
			return sources(Arrays.asList(sources));
		}

		public JacocoReportSpexPlease sources(Collection<? extends Path> sources) {
			this.sources.addAll(sources);
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
		ingredients.add(jacoco);
		ingredients.addAll(deps);
		ingredients.addAll(antJars);
		ingredients.addAll(coverages);
		ingredients.addAll(classes);
		ingredients.addAll(sources);
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append("\n");
		b.append("jacoco:").append(jacoco).append("\n");
		b.append("deps:").append(deps).append("\n");
		b.append("antJars:").append(antJars).append("\n");
		b.append("coverages:").append(coverages).append("\n");
		b.append("classes:").append(classes).append("\n");
		b.append("sources:").append(sources).append("\n");
		return b.toString();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		ctx.cached(this).mkdirs();
		File tmp = ctx.freshTemporaryDirectory();

		File antScript = new File(tmp, name() + ".xml");
		FileUtils.writeStringToFile(antScript, antScript(ctx), "UTF-8");

		List<File> cachedAntJars = new ArrayList<File>();
		for (Path antJar : antJars) {
			cachedAntJars.add(ctx.cached(antJar));
		}
		AntGenerated.runAnt(cachedAntJars, antScript);
	}

	private String antScript(TargetEvaluationContext ctx) {
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
		b.append("		<property name=\"report-dir\" location=\""
				+ ctx.cached(this) + "\" />\n");
		b.append("		<echo message=\"Generating ${report-dir}\" />\n");
		b.append("		<jacoco:report>\n");
		b.append("			<executiondata>\n");
		for (Path coverage : coverages) {
			File cachedCoverage = ctx.cached(coverage);
			if (!cachedCoverage.exists()) {
				// jacoco is like emma here, omitting exec when zero coverage
				continue;
			}
			b.append("				<file file=\"" + cachedCoverage + "\" />\n");
		}
		b.append("			</executiondata>\n");
		b.append("\n");
		b.append("			<structure name=\"" + name() + "\">\n");
		b.append("				<classfiles>\n");
		for (Path classLoc : classes) {
			b.append("					<fileset dir=\"" + ctx.cached(classLoc) + "\" />\n");
		}
		b.append("				</classfiles>\n");
		b.append("				<sourcefiles encoding=\"UTF-8\">\n");
		for (Path source : sources) {
			b.append("					<fileset dir=\"" + ctx.cached(source) + "\" />\n");
		}
		b.append("				</sourcefiles>\n");
		b.append("			</structure>\n");
		b.append("\n");
		b.append("			<html destdir=\"${report-dir}\" />\n");
		b.append("			<csv destfile=\"${report-dir}/report.csv\" />\n");
		b.append("			<xml destfile=\"${report-dir}/report.xml\" />\n");
		b.append("		</jacoco:report>\n");
		b.append("	</target>\n");

		b.append("\n");
		b.append("</project>\n");
		return b.toString();
	}

}
