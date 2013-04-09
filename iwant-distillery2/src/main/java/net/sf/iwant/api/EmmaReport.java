package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class EmmaReport extends Target {

	private Path emma;
	private Set<EmmaInstrumentation> instrumentations;
	private Set<EmmaCoverage> coverages;

	public EmmaReport(String name, Path emma,
			Set<EmmaInstrumentation> instrumentations,
			Set<EmmaCoverage> coverages) {
		super(name);
		this.emma = emma;
		this.instrumentations = instrumentations;
		this.coverages = coverages;
	}

	public static EmmaReportSpex with() {
		return new EmmaReportSpex();
	}

	public static class EmmaReportSpex {

		private String name;
		private Path emma;
		private final Set<EmmaInstrumentation> instrumentations = new LinkedHashSet<EmmaInstrumentation>();
		private final Set<EmmaCoverage> coverages = new LinkedHashSet<EmmaCoverage>();

		public EmmaReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public EmmaReportSpex emma(Path emma) {
			this.emma = emma;
			return this;
		}

		public EmmaReportSpex instrumentations(
				EmmaInstrumentation... instrumentations) {
			return instrumentations(Arrays.asList(instrumentations));
		}

		public EmmaReportSpex instrumentations(
				Collection<? extends EmmaInstrumentation> instrumentations) {
			this.instrumentations.addAll(instrumentations);
			return this;
		}

		public EmmaReportSpex coverages(EmmaCoverage... coverages) {
			return coverages(Arrays.asList(coverages));
		}

		public EmmaReportSpex coverages(
				Collection<? extends EmmaCoverage> coverages) {
			this.coverages.addAll(coverages);
			return this;
		}

		public EmmaReport end() {
			return new EmmaReport(name, emma, instrumentations, coverages);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);

		File reportTxt = new File(dest, "coverage.txt");
		File reportHtml = new File(dest, "coverage/index.html");
		File reportProps = Iwant.newTextFile(
				new File(dest, "emma-report.properties"),
				"report.txt.out.file=" + reportTxt.getCanonicalPath()
						+ "\nreport.html.out.file="
						+ reportHtml.getCanonicalPath() + "\n");

		List<String> reportArgs = new ArrayList<String>();
		reportArgs.add("report");
		reportArgs.add("-r");
		reportArgs.add("txt,html");
		reportArgs.add("-properties");
		reportArgs.add(reportProps.getCanonicalPath());
		for (EmmaInstrumentation instr : instrumentations) {
			File em = instr.metadataFile(ctx);
			if (!em.exists()) {
				// the whole module seems to be filtered out
				continue;
			}
			reportArgs.add("-in");
			reportArgs.add(em.getCanonicalPath());
			for (Path source : instr.classesAndSources().sources()) {
				reportArgs.add("-sp");
				reportArgs.add(ctx.cached(source).getCanonicalPath());
			}
		}
		for (EmmaCoverage coverage : coverages) {
			reportArgs.add("-in");
			reportArgs.add(coverage.coverageFile(ctx).getCanonicalPath());
		}

		File emmaJar = ctx.cached(emma);
		EmmaInstrumentation.runEmma(emmaJar, reportArgs.toArray(new String[0]));
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.add(emma);
		ingredients.addAll(instrumentations);
		ingredients.addAll(coverages);
		return ingredients;
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
