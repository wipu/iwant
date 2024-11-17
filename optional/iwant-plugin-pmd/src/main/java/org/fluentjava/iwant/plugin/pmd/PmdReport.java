package org.fluentjava.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.entry.Iwant;

import net.sourceforge.pmd.ant.Formatter;
import net.sourceforge.pmd.ant.PMDTask;
import net.sourceforge.pmd.ant.RuleSetWrapper;

public class PmdReport extends TargetBase {

	private final List<Path> srcDirectories;
	private final Path ruleset;

	private PmdReport(String name, List<Path> srcDirectories, Path ruleset) {
		super(name);
		this.srcDirectories = srcDirectories;
		this.ruleset = ruleset;
	}

	public static PmdReportSpex with() {
		return new PmdReportSpex();
	}

	public static class PmdReportSpex {

		private String name;
		private final List<Path> srcDirectories = new ArrayList<>();
		private Path ruleset;

		public PmdReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public PmdReportSpex from(Path... srcDirectories) {
			return from(Arrays.asList(srcDirectories));
		}

		public PmdReportSpex from(Collection<? extends Path> srcDirectories) {
			this.srcDirectories.addAll(srcDirectories);
			return this;
		}

		public PmdReportSpex ruleset(Path ruleset) {
			this.ruleset = ruleset;
			return this;
		}

		public PmdReport end() {
			return new PmdReport(name, srcDirectories, ruleset);
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
				.optionalIngredients("ruleset", ruleset).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		Iwant.mkdirs(dest);

		File rulesetXml = rulesetFile(ctx, dest);

		PMDTask task = new PMDTask();
		task.setProject(new Project());

		RuleSetWrapper rulesetWrapper = new RuleSetWrapper();
		rulesetWrapper.addText(rulesetXml.getCanonicalPath());
		task.addRuleset(rulesetWrapper);

		Formatter htmlFormatter = new Formatter();
		htmlFormatter.setType("html");
		File htmlReport = new File(dest, name() + ".html");
		htmlFormatter.setToFile(htmlReport);
		task.addFormatter(htmlFormatter);

		Formatter textFormatter = new Formatter();
		textFormatter.setType("text");
		File textReport = new File(dest, name() + ".txt");
		textFormatter.setToFile(textReport);
		task.addFormatter(textFormatter);

		Formatter xmlFormatter = new Formatter();
		xmlFormatter.setType("xml");
		File xmlReport = new File(dest, name() + ".xml");
		xmlFormatter.setToFile(xmlReport);
		task.addFormatter(xmlFormatter);

		for (Path srcDirectory : srcDirectories) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(ctx.cached(srcDirectory));
			fileSet.setIncludes("**/*.java");
			task.addFileset(fileSet);
		}

		System.err.println("Running PMD on " + srcDirectories);
		task.execute();
	}

	private File rulesetFile(TargetEvaluationContext ctx, File dest)
			throws IOException {
		if (ruleset == null) {
			return generatedRuleset(dest);
		} else {
			return ctx.cached(ruleset);
		}
	}

	private File generatedRuleset(File dest) throws IOException {
		File generated = new File(dest, "ruleset.xml");
		FileUtils.writeStringToFile(generated, rulesetXml(),
				StandardCharsets.UTF_8);
		return generated;
	}

	private String rulesetXml() {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\"?>\n");
		b.append(
				"<ruleset name=\"SNC ruleset\" xmlns=\"http://pmd.sf.net/ruleset/1.0.0\"\n");
		b.append("	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		b.append(
				"	xsi:schemaLocation=\"http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd\"\n");
		b.append(
				"	xsi:noNamespaceSchemaLocation=\"http://pmd.sf.net/ruleset_xml_schema.xsd\">\n");
		b.append("\n");
		b.append("	<description>PMD rules for " + name() + "</description>\n");
		b.append("\n");
		b.append("	<rule ref=\"rulesets/internal/all-java.xml\" />\n");
		b.append("\n");
		b.append("</ruleset>\n");
		return b.toString();
	}

}
