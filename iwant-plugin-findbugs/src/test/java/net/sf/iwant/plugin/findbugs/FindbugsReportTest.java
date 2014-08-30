package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.AsEmbeddedIwantUser;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.entry.Iwant;

import org.apache.commons.io.FileUtils;

public class FindbugsReportTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		caches.cachesUrlAt(distroToTest().tarGz().url(), cachedFindbugsTarGz());
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cached).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution.ofVersion("2.0.2");
	}

	private static File cachedFindbugsTarGz() {
		return Iwant.usingRealNetwork()
				.downloaded(distroToTest().tarGz().url());
	}

	protected String htmlReportContent(Target report) throws IOException {
		return reportContent(report, "html");
	}

	protected String xmlReportContent(Target report) throws IOException {
		return reportContent(report, "xml");
	}

	protected String textReportContent(Target report) throws IOException {
		return reportContent(report, "text");
	}

	protected String reportContent(Target report, String extension)
			throws IOException {
		File reportFile = new File(ctx.cached(report), "findbugs-report/"
				+ report.name() + "." + extension);
		if (!reportFile.exists()) {
			return null;
		}
		String reportFileContent = FileUtils.readFileToString(reportFile);
		return reportFileContent;
	}

	protected void srcDirHasFindbugsFodder(File srcDir,
			String lastPartOfPackage, String javaClassName) throws IOException {
		final String packageDirName = "net/sf/iwant/plugin/findbugs/"
				+ lastPartOfPackage;
		File packageDir = new File(srcDir, packageDirName);
		packageDir.mkdirs();

		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaClassName + ".txt")),
				new File(packageDir, javaClassName + ".java"));
	}

	// -----------------------------------------------------
	// the tests
	// -----------------------------------------------------

	public void testDistroMustBeGiven() {
		wsRootHasDirectory("empty-src");
		wsRootHasDirectory("empty-classes");
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");

		try {
			FindbugsReport
					.with()
					.name("distro-missing")
					.classesToAnalyze(
							new JavaClassesAndSources(emptyClasses, emptySrc))
					.end();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Please specify the findbugs distribution to use.",
					e.getMessage());
		}
	}

	public void testIngredientsAndContentDescriptor() throws IOException {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Path bin = Source.underWsroot("bin.jar");

		Target report = FindbugsReport
				.with()
				.name("fb-empty")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.auxClasses(bin).end();

		assertEquals("[findbugs-2.0.2, " + antJar() + ", " + antLauncherJar()
				+ ", empty-classes, empty-src, bin.jar]", report.ingredients()
				.toString());
		assertEquals("net.sf.iwant.plugin.findbugs.FindbugsReport {\n"
				+ "  output-format:html\n" + "  ingredients: {\n"
				+ "    findbugs-2.0.2\n" + "    " + antJar() + "\n" + "    "
				+ antLauncherJar() + "\n" + "    empty-classes\n"
				+ "    empty-src\n    bin.jar\n" + "  }\n"
				+ "  classesToAnalyze: {\n"
				+ "    JavaClassesAndSources {empty-classes [empty-src]}\n"
				+ "  }\n  auxClasses: {\n" + "    bin.jar\n" + "  }\n" + ""
				+ "}\n" + "", report.contentDescriptor());
	}

	public void testExplicitHtmlOutputFormat() throws IOException {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Target report = FindbugsReport
				.with()
				.name("fb-empty")
				.outputFormat(FindbugsOutputFormat.HTML)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		assertTrue(report.contentDescriptor().contains("output-format:html"));
	}

	public void testContentDescriptorWithXmlOutputFormat() throws IOException {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Target report = FindbugsReport
				.with()
				.name("fb-empty")
				.outputFormat(FindbugsOutputFormat.XML)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		assertEquals("net.sf.iwant.plugin.findbugs.FindbugsReport {\n"
				+ "  output-format:xml\n" + "  ingredients: {\n"
				+ "    findbugs-2.0.2\n" + "    " + antJar() + "\n" + "    "
				+ antLauncherJar() + "\n" + "    empty-classes\n"
				+ "    empty-src\n" + "  }\n" + "  classesToAnalyze: {\n"
				+ "    JavaClassesAndSources {empty-classes [empty-src]}\n"
				+ "  }\n" + "  auxClasses: {\n" + "  }\n" + "}\n" + "",
				report.contentDescriptor());
	}

	public void testDefaultReportFromEmptyClasses() throws Exception {
		wsRootHasDirectory("empty-src");
		wsRootHasDirectory("empty-classes");
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");

		distroToTest().path(ctx);

		Target report = FindbugsReport
				.with()
				.name("fb-empty")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();
		report.path(ctx);

		assertTrue(new File(cached, "fb-empty").exists());
		String htmlReportContent = htmlReportContent(report);
		assertEquals("", htmlReportContent);
	}

	public void testReportMentionsIssuesFromTheGivenClass() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder", "ClassWithFindbugsIssues");

		Source src = Source.underWsroot("src");
		JavaClasses classes = JavaClasses.with().name("classes").srcDirs(src)
				.end();
		classes.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with().name("oneclass-report")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes, src))
				.end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertTrue(htmlReportContent
				.contains("<td>Null pointer dereference of ? in "
						+ "net.sf.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues."
						+ "nullReference(Object)</td>"));
	}

	public void testReportDoesNotDetectProblemIfDependencyNotInAuxclasses()
			throws Exception {
		File src1Dir = new File(wsRoot, "src1");
		srcDirHasFindbugsFodder(src1Dir, "testfodder",
				"ClassWithBugUsingBinaryDependency");
		File src2Dir = new File(wsRoot, "src2");
		srcDirHasFindbugsFodder(src2Dir, "testfodder2", "BinaryDependency");

		Source src2 = Source.underWsroot("src2");
		JavaClasses classes2 = JavaClasses.with().name("classes2")
				.srcDirs(src2).end();
		classes2.path(ctx);
		Source src1 = Source.underWsroot("src1");
		JavaClasses classes1 = JavaClasses.with().name("classes1")
				.srcDirs(src1).classLocations(classes2).end();
		classes1.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with()
				.name("without-auxclasses")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes1, src1))
				.end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertFalse(htmlReportContent
				.contains(warningAboutBugUsingBinaryDependency()));
	}

	private static final String warningAboutBugUsingBinaryDependency() {
		return "<td>Null pointer dereference of "
				+ "net.sf.iwant.plugin.findbugs.testfodder2.BinaryDependency.NULL_STRING "
				+ "in net.sf.iwant.plugin.findbugs.testfodder.ClassWithBugUsingBinaryDependency."
				+ "nullReferenceOfValueFromBinaryDependency()</td>";
	}

	public void testReportDetectsProblemIfDependencyIsInAuxclasses()
			throws Exception {
		File src1Dir = new File(wsRoot, "src1");
		srcDirHasFindbugsFodder(src1Dir, "testfodder",
				"ClassWithBugUsingBinaryDependency");
		File src2Dir = new File(wsRoot, "src2");
		srcDirHasFindbugsFodder(src2Dir, "testfodder2", "BinaryDependency");

		Source src2 = Source.underWsroot("src2");
		JavaClasses classes2 = JavaClasses.with().name("classes2")
				.srcDirs(src2).end();
		classes2.path(ctx);
		Source src1 = Source.underWsroot("src1");
		JavaClasses classes1 = JavaClasses.with().name("classes1")
				.srcDirs(src1).classLocations(classes2).end();
		classes1.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with().name("with-auxclasses")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes1, src1))
				.auxClasses(classes2).end();
		report.path(ctx);

		String htmlReportContent = htmlReportContent(report);
		assertTrue(htmlReportContent
				.contains(warningAboutBugUsingBinaryDependency()));
	}

	public void testReportUsesXmlFormatWhenExplicitlyRequested()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder", "ClassWithFindbugsIssues");

		Source src = Source.underWsroot("src");
		JavaClasses classes = JavaClasses.with().name("classes").srcDirs(src)
				.end();
		classes.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with().name("oneclass-report")
				.outputFormat(FindbugsOutputFormat.XML)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes, src))
				.end();
		report.path(ctx);

		String xmlReportContent = xmlReportContent(report);
		assertTrue(xmlReportContent
				.contains("<Method classname="
						+ "\"net.sf.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues\""
						+ " name=\"nullReference\""));
	}

	public void testReportUsesTextFormatWhenExplicitlyRequested()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder", "ClassWithFindbugsIssues");

		Source src = Source.underWsroot("src");
		JavaClasses classes = JavaClasses.with().name("classes").srcDirs(src)
				.end();
		classes.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with().name("oneclass-report")
				.outputFormat(FindbugsOutputFormat.TEXT)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes, src))
				.end();
		report.path(ctx);

		String textReportContent = textReportContent(report);
		System.err.println(textReportContent);
		assertTrue(textReportContent
				.contains("H C NP: Null pointer dereference of ? in "
						+ "net.sf.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues.nullReference(Object)"
						+ "  Dereferenced at ClassWithFindbugsIssues.java:[line 7]\n"));
	}

	public void testModulesToAnalyzeMeansSrcsAnalyzedUsingBins()
			throws IOException {
		JavaBinModule bin1 = JavaBinModule
				.providing(Source.underWsroot("bin1")).end();

		JavaSrcModule javaless = JavaSrcModule.with().name("javaless").end();
		JavaSrcModule mainless = JavaSrcModule.with().name("mainless")
				.testJava("test").end();
		JavaSrcModule testless = JavaSrcModule.with().name("testless")
				.mainJava("src").end();
		JavaSrcModule modWithTestRuntimeDeps = JavaSrcModule.with()
				.name("modWithTestRuntimeDeps").mainJava("src")
				.testJava("test").testRuntimeDeps(bin1).end();

		FindbugsReport report = FindbugsReport
				.with()
				.name("with-auxclasses")
				.using(distroToTest(), antJar(), antLauncherJar())
				.modulesToAnalyze(javaless, mainless, testless,
						modWithTestRuntimeDeps).end();

		assertEquals(
				"[JavaClassesAndSources {mainless-test-classes [mainless/test]},"
						+ " JavaClassesAndSources {testless-main-classes [testless/src]},"
						+ " JavaClassesAndSources {modWithTestRuntimeDeps-main-classes [modWithTestRuntimeDeps/src]},"
						+ " JavaClassesAndSources {modWithTestRuntimeDeps-test-classes [modWithTestRuntimeDeps/test]}]",
				report.classesToAnalyze().toString());
		assertEquals("[bin1]", report.auxClasses().toString());
	}

}
