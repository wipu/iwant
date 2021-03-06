package org.fluentjava.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.entry.Iwant;

public class PmdReportTest extends PmdTestBase {

	private String htmlReportContent(PmdReport report) throws IOException {
		return reportContent(report, "html");
	}

	// -----------------------------------------------------
	// the tests
	// -----------------------------------------------------

	public void testSourceDirectoriesAreIngredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src2");

		PmdReport report = PmdReport.with().name("pmd-report").from(src1, src2)
				.end();

		assertTrue(report.ingredients().contains(src1));
		assertTrue(report.ingredients().contains(src2));
	}

	public void testSourceDirectoriesAreInContentDescriptor() {
		Path src1 = Source.underWsroot("src-one");
		Path src2 = Source.underWsroot("src-two");

		PmdReport report = PmdReport.with().name("pmd-report").from(src1, src2)
				.end();

		assertTrue(report.contentDescriptor().contains(src1.name()));
		assertTrue(report.contentDescriptor().contains(src2.name()));
	}

	public void testRulesetIsInIngredientsIffDefined() {
		Path src = Source.underWsroot("src");
		Path rules = Source.underWsroot("rules.xml");

		assertTrue(PmdReport.with().name("with").from(src).ruleset(rules).end()
				.ingredients().contains(rules));
		// no "null" here:
		assertEquals("[src]", PmdReport.with().name("without").from(src).end()
				.ingredients().toString());
	}

	public void testReportOfZeroSrcDirectoriesProducesReportFiles()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		PmdReport report = PmdReport.with().name("pmd-report").end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() > 0);
	}

	public void testReportOfEmptySrcDirectoryProducesReportFiles()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() > 0);
	}

	public void testAllReportFormatsOfOneClassesDirWithOneClassWithIssues()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasPmdFodder(srcDir, "testfodder", "ClassWithPmdIssues");

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertTrue(htmlReportContent
				.contains("Avoid reassigning parameters such as 'parameter'"));

		String txtReportContent = txtReportContent(report);
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:5"
						+ "	Avoid reassigning parameters such as 'parameter'"));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:9"
						+ "	Method names should not start with capital letters"));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:13"
						+ "	Avoid unused private methods such as 'deadMethod()'."));
		assertTrue(txtReportContent.contains(
				"org/fluentjava/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:17"
						+ "	Document empty method"));

		String xmlReportContent = xmlReportContent(report);
		assertTrue(xmlReportContent.contains("ClassWithPmdIssues.java\">\n"
				+ "<violation beginline=\"5\" endline=\"5\""
				+ " begincolumn=\"54\" endcolumn=\"62\""
				+ " rule=\"AvoidReassigningParameters\""));
	}

	public void testReportOnlyContainsIssuesDefinedInGivenRulesPath()
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
				"Method names should not start with capital letters"));
		assertFalse(txtReportContent.contains(
				"Avoid unused private methods such as 'deadMethod()'."));
		assertFalse(txtReportContent.contains("Document empty method"));
	}

}
