package org.fluentjava.iwant.plugin.pmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.entry.Iwant;
import org.junit.jupiter.api.Test;

public class PmdReportTest extends PmdTestBase {

	private String htmlReportContent(PmdReport report) throws IOException {
		return reportContent(report, "html");
	}

	// -----------------------------------------------------
	// the tests
	// -----------------------------------------------------

	@Test
	public void sourceDirectoriesAreIngredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src2");

		PmdReport report = PmdReport.with().name("pmd-report").from(src1, src2)
				.end();

		assertTrue(report.ingredients().contains(src1));
		assertTrue(report.ingredients().contains(src2));
	}

	@Test
	public void sourceDirectoriesAreInContentDescriptor() {
		Path src1 = Source.underWsroot("src-one");
		Path src2 = Source.underWsroot("src-two");

		PmdReport report = PmdReport.with().name("pmd-report").from(src1, src2)
				.end();

		assertTrue(report.contentDescriptor().contains(src1.name()));
		assertTrue(report.contentDescriptor().contains(src2.name()));
	}

	@Test
	public void rulesetIsInIngredientsIffDefined() {
		Path src = Source.underWsroot("src");
		Path rules = Source.underWsroot("rules.xml");

		assertTrue(PmdReport.with().name("with").from(src).ruleset(rules).end()
				.ingredients().contains(rules));
		// no "null" here:
		assertEquals("[src]", PmdReport.with().name("without").from(src).end()
				.ingredients().toString());
	}

	@Test
	public void reportOfZeroSrcDirectoriesProducesReportFiles()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		PmdReport report = PmdReport.with().name("pmd-report").end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() == 0);
	}

	@Test
	public void reportOfEmptySrcDirectoryProducesReportFiles()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() == 0);
	}

	@Test
	public void allReportFormatsOfOneClassesDirWithOneClassWithIssues()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasPmdFodder(srcDir, "testfodder", "ClassWithPmdIssues");

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertTrue(htmlReportContent.contains(
				"The initial value of parameter 'parameter' is never used"));

		String txtReportContent = txtReportContent(report);
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:6:"
						+ "	AvoidReassigningParameters:	Avoid reassigning parameters such as 'parameter'"));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:9:"
						+ "	MethodNamingConventions:	The instance method name 'MethodWithDiscouragedName'"
						+ " doesn't match '[a-z][a-zA-Z0-9]*"));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:13:"
						+ "	UnusedPrivateMethod:	Avoid unused private methods such as 'deadMethod()"));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:17:"
						+ "	UncommentedEmptyMethodBody:	Document empty method body"));

		String xmlReportContent = xmlReportContent(report);
		assertTrue(xmlReportContent.contains(
				"<violation beginline=\"6\" endline=\"6\" begincolumn=\"3\""
						+ " endcolumn=\"12\" rule=\"AvoidReassigningParameters\" ruleset=\"Best Practices\""
						+ " package=\"net.sf.iwant.plugin.pmd.testfodder\" class=\"ClassWithPmdIssues\""
						+ " method=\"methodThatWritesParameter\""));
	}

	@Test
	public void reportOnlyContainsIssuesDefinedInGivenRulesPath()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasPmdFodder(srcDir, "testfodder", "ClassWithPmdIssues");

		FileUtils.copyFile(
				FileUtils.toFile(
						getClass().getResource("ruleset-with-naming-only.xml")),
				new File(wsRoot, "custom-ruleset.xml"));

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src"))
				.ruleset(Source.underWsroot("custom-ruleset.xml")).end();
		report.path(ctx);

		String txtReportContent = txtReportContent(report);
		assertFalse(txtReportContent
				.contains("Avoid reassigning parameters such as 'parameter'"));
		assertTrue(txtReportContent.contains(
				"The instance method name 'MethodWithDiscouragedName'"));
		assertFalse(txtReportContent.contains(
				"Avoid unused private methods such as 'deadMethod()'."));
		assertFalse(txtReportContent.contains("Document empty method"));
	}

}
