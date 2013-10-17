package net.sf.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

import org.apache.commons.io.FileUtils;

public class PmdReportTest extends TestCase {

	private IwantPluginPmdTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginPmdTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cached);
	}

	private String htmlReportContent(PmdReport report) throws IOException {
		return reportContent(report, "html");
	}

	private String txtReportContent(PmdReport report) throws IOException {
		return reportContent(report, "txt");
	}

	private String reportContent(PmdReport report, String extension)
			throws IOException {
		File reportFile = new File(ctx.cached(report), "pmd-report."
				+ extension);
		assertTrue(reportFile.exists());
		String reportFileContent = FileUtils.readFileToString(reportFile);
		return reportFileContent;
	}

	private void srcDirHasPmdFodder(File srcDir) throws IOException {
		final String packageDirName = "net/sf/iwant/plugin/pmd/testfodder";
		File packageDir = new File(srcDir, packageDirName);
		packageDir.mkdirs();

		String javaFileName = "ClassWithPmdIssues";
		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaFileName + ".txt")),
				new File(packageDir, javaFileName + ".java"));
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
		srcDir.mkdirs();

		PmdReport report = PmdReport.with().name("pmd-report").end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() > 0);
	}

	public void testReportOfEmptySrcDirectoryProducesReportFiles()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		assertTrue(htmlReportContent(report).length() > 0);
		assertTrue(txtReportContent(report).length() > 0);
	}

	public void testReportOfOneClassesDirWithOneClassWithIssues()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		srcDirHasPmdFodder(srcDir);

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertTrue(htmlReportContent
				.contains("Avoid reassigning parameters such as 'parameter'"));

		String txtReportContent = txtReportContent(report);
		assertTrue(txtReportContent
				.contains("net/sf/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:5"
						+ "	Avoid reassigning parameters such as 'parameter'"));
		assertTrue(txtReportContent
				.contains("net/sf/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:9"
						+ "	Method names should not start with capital letters"));
		assertTrue(txtReportContent
				.contains("net/sf/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:13"
						+ "	Avoid unused private methods such as 'deadMethod()'."));
		assertTrue(txtReportContent
				.contains("net/sf/iwant/plugin/pmd/testfodder/ClassWithPmdIssues.java:17"
						+ "	Document empty method"));
	}

	public void testReportOnlyContainsIssuesDefinedInGivenRulesPath()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		srcDirHasPmdFodder(srcDir);

		FileUtils.copyFile(FileUtils.toFile(getClass().getResource(
				"ruleset-with-naming-only.xml")), new File(wsRoot,
				"custom-ruleset.xml"));

		PmdReport report = PmdReport.with().name("pmd-report")
				.from(Source.underWsroot("src"))
				.ruleset(Source.underWsroot("custom-ruleset.xml")).end();
		report.path(ctx);

		String txtReportContent = txtReportContent(report);
		assertFalse(txtReportContent
				.contains("Avoid reassigning parameters such as 'parameter'"));
		assertTrue(txtReportContent
				.contains("Method names should not start with capital letters"));
		assertFalse(txtReportContent
				.contains("Avoid unused private methods such as 'deadMethod()'."));
		assertFalse(txtReportContent.contains("Document empty method"));
	}

}
