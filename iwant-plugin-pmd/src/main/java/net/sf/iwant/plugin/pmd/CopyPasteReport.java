package net.sf.iwant.plugin.pmd;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sourceforge.pmd.cpd.CPDTask;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

public class CopyPasteReport extends Target {

	private final List<Path> srcDirectories;
	private final int minimumTokenCount;

	private CopyPasteReport(String name, List<Path> srcDirectories,
			int minimumTokenCount) {
		super(name);
		this.srcDirectories = srcDirectories;
		this.minimumTokenCount = minimumTokenCount;
	}

	public static CopyPasteReportSpex with() {
		return new CopyPasteReportSpex();
	}

	public static class CopyPasteReportSpex {

		private String name;
		private final List<Path> srcDirectories = new ArrayList<Path>();
		private int minimumTokenCount = 100;

		public CopyPasteReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public CopyPasteReportSpex from(Path... classes) {
			return from(Arrays.asList(classes));
		}

		public CopyPasteReportSpex from(
				Collection<? extends Path> srcDirectories) {
			this.srcDirectories.addAll(srcDirectories);
			return this;
		}

		public CopyPasteReportSpex minimumTokenCount(int minimumTokenCount) {
			this.minimumTokenCount = minimumTokenCount;
			return this;
		}

		public CopyPasteReport end() {
			return new CopyPasteReport(name, srcDirectories, minimumTokenCount);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<Path>();
		ingredients.addAll(srcDirectories);
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		if (srcDirectories.isEmpty()) {
			// PMD does not tolerate this so let's give an error message on our
			// abstraction level:
			throw new IllegalArgumentException("No source directories given.");
		}
		File dest = ctx.cached(this);
		dest.mkdirs();

		File txtReport = new File(dest, name() + ".txt");

		CPDTask task = new CPDTask();
		task.setProject(new Project());
		task.setMinimumTokenCount(minimumTokenCount);
		task.setIgnoreIdentifiers(true);
		task.setIgnoreLiterals(true);
		task.setOutputFile(txtReport);

		for (Path srcDirectory : srcDirectories) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(ctx.cached(srcDirectory));
			fileSet.setIncludes("**/*.java");
			task.addFileset(fileSet);
		}

		System.err.println("Running copy-paste analysis on " + srcDirectories);
		task.execute();
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  ingredients {\n");
		for (Path ingredient : ingredients()) {
			b.append("    ").append(ingredient).append("\n");
		}
		b.append("  }\n");
		b.append("  minimumTokenCount:" + minimumTokenCount + "\n");
		b.append("}\n");
		return b.toString();
	}

}
