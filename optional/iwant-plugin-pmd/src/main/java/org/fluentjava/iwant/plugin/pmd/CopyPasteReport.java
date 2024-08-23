package org.fluentjava.iwant.plugin.pmd;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;

import net.sourceforge.pmd.ant.CPDTask;

public class CopyPasteReport extends TargetBase {

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
		private final List<Path> srcDirectories = new ArrayList<>();
		private int minimumTokenCount = 100;

		public CopyPasteReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public CopyPasteReportSpex from(Path... srcDirectories) {
			return from(Arrays.asList(srcDirectories));
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
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("srcDirectories", srcDirectories)
				.parameter("minimumTokenCount", minimumTokenCount)
				.nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		if (srcDirectories.isEmpty()) {
			// PMD does not tolerate this so let's give an error message on our
			// abstraction level:
			throw new IwantException("No source directories given.");
		}
		File dest = ctx.cached(this);
		Iwant.mkdirs(dest);

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

}
