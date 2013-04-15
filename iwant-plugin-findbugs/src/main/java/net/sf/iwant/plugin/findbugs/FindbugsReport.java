package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import edu.umd.cs.findbugs.FindBugs;

public class FindbugsReport extends Target {

	private final List<JavaClassesAndSources> classesToAnalyze;

	public FindbugsReport(String name,
			List<JavaClassesAndSources> classesToAnalyze) {
		super(name);
		this.classesToAnalyze = classesToAnalyze;
	}

	public static FindbugsReportSpex with() {
		return new FindbugsReportSpex();
	}

	public static class FindbugsReportSpex {

		private String name;
		private final List<JavaClassesAndSources> classesToAnalyze = new ArrayList<JavaClassesAndSources>();

		public FindbugsReport end() {
			return new FindbugsReport(name, classesToAnalyze);
		}

		public FindbugsReportSpex name(String name) {
			this.name = name;
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
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File outputFile = new File(dest, "findbugs");

		List<String> args = new ArrayList<String>();
		List<File> cachedClassesToAnalyze = new ArrayList<File>();
		args.add("-outputFile");
		args.add(outputFile.getCanonicalPath());
		for (JavaClassesAndSources classesAndSrc : classesToAnalyze) {
			for (Path src : classesAndSrc.sources()) {
				File cachedSrc = ctx.cached(src);
				args.add("-sourcepath");
				args.add(cachedSrc.getCanonicalPath());
			}
			File cachedClasses = ctx.cached(classesAndSrc.classes());
			cachedClassesToAnalyze.add(cachedClasses);
		}
		for (File classes : cachedClassesToAnalyze) {
			args.add(classes.getCanonicalPath());
		}
		try {
			System.err.println("running");
			FindBugs.main(args.toArray(new String[0]));
		} finally {
			System.err.println("hää");
		}
	}

	@Override
	public String contentDescriptor() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
