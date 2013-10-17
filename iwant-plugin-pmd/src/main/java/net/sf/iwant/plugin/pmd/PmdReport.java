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
import net.sourceforge.pmd.ant.Formatter;
import net.sourceforge.pmd.ant.PMDTask;
import net.sourceforge.pmd.ant.RuleSetWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

public class PmdReport extends Target {

	private final List<Path> srcDirectories;

	private PmdReport(String name, List<Path> srcDirectories) {
		super(name);
		this.srcDirectories = srcDirectories;
	}

	public static PmdReportSpex with() {
		return new PmdReportSpex();
	}

	public static class PmdReportSpex {

		private String name;
		private final List<Path> srcDirectories = new ArrayList<Path>();

		public PmdReportSpex name(String name) {
			this.name = name;
			return this;
		}

		public PmdReportSpex from(Path... classes) {
			return from(Arrays.asList(classes));
		}

		public PmdReportSpex from(Collection<? extends Path> srcDirectories) {
			this.srcDirectories.addAll(srcDirectories);
			return this;
		}

		public PmdReport end() {
			return new PmdReport(name, srcDirectories);
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
		File dest = ctx.cached(this);
		dest.mkdirs();

		File rulesetXml = new File(dest, "ruleset.xml");
		FileUtils.writeStringToFile(rulesetXml, rulesetXml());

		PMDTask task = new PMDTask();
		task.setProject(new Project());
		task.setShortFilenames(true);

		RuleSetWrapper ruleset = new RuleSetWrapper();
		ruleset.addText(rulesetXml.getCanonicalPath());
		task.addRuleset(ruleset);

		Formatter htmlFormatter = new Formatter();
		htmlFormatter.setType("betterhtml");
		File htmlReport = new File(dest, name() + ".html");
		htmlFormatter.setToFile(htmlReport);
		task.addFormatter(htmlFormatter);

		Formatter textFormatter = new Formatter();
		textFormatter.setType("text");
		File textReport = new File(dest, name() + ".txt");
		textFormatter.setToFile(textReport);
		task.addFormatter(textFormatter);

		for (Path srcDirectory : srcDirectories) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(ctx.cached(srcDirectory));
			fileSet.setIncludes("**/*.java");
			task.addFileset(fileSet);
		}

		System.err.println("Running PMD on " + srcDirectories.size()
				+ " source directories.");
		task.execute();
	}

	private String rulesetXml() {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\"?>\n");
		b.append("<ruleset name=\"SNC ruleset\" xmlns=\"http://pmd.sf.net/ruleset/1.0.0\"\n");
		b.append("	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		b.append("	xsi:schemaLocation=\"http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd\"\n");
		b.append("	xsi:noNamespaceSchemaLocation=\"http://pmd.sf.net/ruleset_xml_schema.xsd\">\n");
		b.append("\n");
		b.append("	<description>PMD rules for " + name() + "</description>\n");
		b.append("\n");
		b.append("	<rule ref=\"rulesets/basic.xml\" />\n");
		b.append("	<rule ref=\"rulesets/design.xml\" />\n");
		b.append("	<rule ref=\"rulesets/unusedcode.xml\" />\n");
		b.append("	<rule ref=\"rulesets/naming.xml\" />\n");
		b.append("\n");
		b.append("</ruleset>\n");
		return b.toString();
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  ingredients {\n");
		for (Path ingredient : ingredients()) {
			b.append("    ").append(ingredient);
		}
		b.append("  }\n");
		b.append("}\n");
		return b.toString();
	}

}
