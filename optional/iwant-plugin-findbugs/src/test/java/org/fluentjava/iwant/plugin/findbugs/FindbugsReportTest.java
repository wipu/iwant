package org.fluentjava.iwant.plugin.findbugs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaClassesAndSources;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.embedded.AsEmbeddedIwantUser;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.plugin.findbugs.FindbugsReport.FindbugsReportSpex;
import org.junit.jupiter.api.Test;

public class FindbugsReportTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		caches.cachesUrlAt(distroToTest().tarGz().url(), cachedFindbugsTarGz());
	}

	private Path downloaded(Path downloaded) {
		return ExternalSource.at(AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cached).iwant().target((Target) downloaded).asPath());
	}

	private Path antJar() {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	private static FindbugsDistribution distroToTest() {
		return FindbugsDistribution._4_8_3;
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
		File reportFile = new File(ctx.cached(report),
				"findbugs-report/" + report.name() + "." + extension);
		if (!reportFile.exists()) {
			return null;
		}
		String reportFileContent = FileUtils.readFileToString(reportFile,
				StandardCharsets.UTF_8);
		return reportFileContent;
	}

	protected void srcDirHasFindbugsFodder(File srcDir,
			String lastPartOfPackage, String javaClassName) throws IOException {
		final String packageDirName = "org/fluentjava/iwant/plugin/findbugs/"
				+ lastPartOfPackage;
		File packageDir = new File(srcDir, packageDirName);
		Iwant.mkdirs(packageDir);

		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaClassName + ".txt")),
				new File(packageDir, javaClassName + ".java"));
	}

	// -----------------------------------------------------
	// the tests
	// -----------------------------------------------------

	@Test
	public void distroMustBeGiven() {
		wsRootHasDirectory("empty-src");
		wsRootHasDirectory("empty-classes");
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");

		try {
			FindbugsReport.with().name("distro-missing")
					.classesToAnalyze(
							new JavaClassesAndSources(emptyClasses, emptySrc))
					.end();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Please specify the findbugs distribution to use.",
					e.getMessage());
		}
	}

	@Test
	public void ingredientsAndContentDescriptor() {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Path bin = Source.underWsroot("bin.jar");

		FindbugsReportSpex report = FindbugsReport.with().name("fb-empty")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.auxClasses(bin);

		assertEquals(
				"[spotbugs-4.8.3, " + antJar() + ", " + antLauncherJar()
						+ ", empty-classes, empty-src, bin.jar]",
				report.end().ingredients().toString());
		assertEquals("org.fluentjava.iwant.plugin.findbugs.FindbugsReport\n"
				+ "i:findbugs:\n" + "  spotbugs-4.8.3\n" + "i:antJar:\n" + "  "
				+ antJar() + "\ni:antLauncherJar:\n" + "  " + antLauncherJar()
				+ "\ni:classes:\n" + "  empty-classes\n" + "i:sources:\n"
				+ "  empty-src\n" + "i:auxClasses:\n" + "  bin.jar\n"
				+ "p:output-format:\n" + "  html\n" + "",
				report.end().contentDescriptor());

		// optional ingredient:
		Path excludeXml = Source.underWsroot("exclude.xml");
		report.exclude(excludeXml);
		assertTrue(report.end().ingredients().contains(excludeXml));
		assertTrue(report.end().contentDescriptor()
				.contains("i:excludeFile:\n" + "  exclude.xml\n"));
	}

	@Test
	public void explicitHtmlOutputFormat() {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Target report = FindbugsReport.with().name("fb-empty")
				.outputFormat(FindbugsOutputFormat.HTML)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		assertTrue(
				report.contentDescriptor().contains("output-format:\n  html"));
	}

	@Test
	public void contentDescriptorWithXmlOutputFormat() {
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");
		Target report = FindbugsReport.with().name("fb-empty")
				.outputFormat(FindbugsOutputFormat.XML)
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		assertEquals("org.fluentjava.iwant.plugin.findbugs.FindbugsReport\n"
				+ "i:findbugs:\n" + "  spotbugs-4.8.3\n" + "i:antJar:\n" + "  "
				+ antJar() + "\ni:antLauncherJar:\n" + "  " + antLauncherJar()
				+ "\ni:classes:\n" + "  empty-classes\n" + "i:sources:\n"
				+ "  empty-src\n" + "i:auxClasses:\n" + "p:output-format:\n"
				+ "  xml\n" + "", report.contentDescriptor());
	}

	@Test
	public void defaultReportFromEmptyClasses() throws Exception {
		wsRootHasDirectory("empty-src");
		wsRootHasDirectory("empty-classes");
		Path emptySrc = Source.underWsroot("empty-src");
		Path emptyClasses = Source.underWsroot("empty-classes");

		distroToTest().path(ctx);

		Target report = FindbugsReport.with().name("fb-empty")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();
		report.path(ctx);

		assertTrue(new File(cached, "fb-empty").exists());
		String htmlReportContent = htmlReportContent(report);
		assertEquals("", htmlReportContent);
	}

	@Test
	public void reportMentionsIssuesFromTheGivenClassButOnlyIfNotFiltered()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder",
				"ClassWithFindbugsIssues");

		Source src = Source.underWsroot("src");
		JavaClasses classes = JavaClasses.with().name("classes").srcDirs(src)
				.end();
		classes.path(ctx);

		distroToTest().path(ctx);

		String npeWarningSnippet = "<td>Null pointer dereference of ? in "
				+ "org.fluentjava.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues."
				+ "nullReference(Object)</td>";

		// all rules in use:

		FindbugsReport fullReport = FindbugsReport.with()
				.name("oneclass-report")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes, src))
				.end();
		fullReport.path(ctx);
		assertTrue(htmlReportContent(fullReport).contains(npeWarningSnippet));

		// report with excluded NPE rule:

		wsRootHasFile("exclude.xml", "<FindBugsFilter><Match><Bug pattern=\""
				+ "NP_ALWAYS_NULL\"/></Match></FindBugsFilter>");

		FindbugsReport filteredReport = FindbugsReport.with()
				.name("filtered-oneclass-report")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(classes, src))
				.exclude(Source.underWsroot("exclude.xml")).end();
		filteredReport.path(ctx);
		assertFalse(
				htmlReportContent(filteredReport).contains(npeWarningSnippet));
	}

	@Test
	public void reportDoesNotDetectProblemIfDependencyNotInAuxclasses()
			throws Exception {
		File src1Dir = new File(wsRoot, "src1");
		srcDirHasFindbugsFodder(src1Dir, "testfodder",
				"ClassWithBugUsingBinaryDependency");
		File src2Dir = new File(wsRoot, "src2");
		srcDirHasFindbugsFodder(src2Dir, "testfodder2", "BinaryDependency");

		Source src2 = Source.underWsroot("src2");
		JavaClasses classes2 = JavaClasses.with().name("classes2").srcDirs(src2)
				.end();
		classes2.path(ctx);
		Source src1 = Source.underWsroot("src1");
		JavaClasses classes1 = JavaClasses.with().name("classes1").srcDirs(src1)
				.classLocations(classes2).end();
		classes1.path(ctx);

		distroToTest().path(ctx);

		FindbugsReport report = FindbugsReport.with().name("without-auxclasses")
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
				+ "org.fluentjava.iwant.plugin.findbugs.testfodder2.BinaryDependency.NULL_STRING "
				+ "in org.fluentjava.iwant.plugin.findbugs.testfodder.ClassWithBugUsingBinaryDependency."
				+ "nullReferenceOfValueFromBinaryDependency()</td>";
	}

	@Test
	public void reportDetectsProblemIfDependencyIsInAuxclasses()
			throws Exception {
		File src1Dir = new File(wsRoot, "src1");
		srcDirHasFindbugsFodder(src1Dir, "testfodder",
				"ClassWithBugUsingBinaryDependency");
		File src2Dir = new File(wsRoot, "src2");
		srcDirHasFindbugsFodder(src2Dir, "testfodder2", "BinaryDependency");

		Source src2 = Source.underWsroot("src2");
		JavaClasses classes2 = JavaClasses.with().name("classes2").srcDirs(src2)
				.end();
		classes2.path(ctx);
		Source src1 = Source.underWsroot("src1");
		JavaClasses classes1 = JavaClasses.with().name("classes1").srcDirs(src1)
				.classLocations(classes2).end();
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

	@Test
	public void reportUsesXmlFormatWhenExplicitlyRequested() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder",
				"ClassWithFindbugsIssues");

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
		assertTrue(xmlReportContent.contains("<Method classname="
				+ "\"org.fluentjava.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues\""
				+ " name=\"nullReference\""));
	}

	@Test
	public void reportUsesTextFormatWhenExplicitlyRequested() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDirHasFindbugsFodder(srcDir, "testfodder",
				"ClassWithFindbugsIssues");

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
						+ "org.fluentjava.iwant.plugin.findbugs.testfodder.ClassWithFindbugsIssues.nullReference(Object)"
						+ "  Dereferenced at ClassWithFindbugsIssues.java:[line 7]\n"));
	}

	@Test
	public void modulesToAnalyzeMeansSrcsAnalyzedUsingBins() {
		JavaBinModule bin1 = JavaBinModule.providing(Source.underWsroot("bin1"))
				.end();

		JavaSrcModule javaless = JavaSrcModule.with().name("javaless").end();
		JavaSrcModule mainless = JavaSrcModule.with().name("mainless")
				.testJava("test").end();
		JavaSrcModule testless = JavaSrcModule.with().name("testless")
				.mainJava("src").end();
		JavaSrcModule modWithTestRuntimeDeps = JavaSrcModule.with()
				.name("modWithTestRuntimeDeps").mainJava("src").testJava("test")
				.testRuntimeDeps(bin1).end();

		FindbugsReport report = FindbugsReport.with().name("with-auxclasses")
				.using(distroToTest(), antJar(), antLauncherJar())
				.modulesToAnalyze(javaless, mainless, testless,
						modWithTestRuntimeDeps)
				.end();

		assertEquals(
				"[JavaClassesAndSources {mainless-test-classes [mainless/test]},"
						+ " JavaClassesAndSources {testless-main-classes [testless/src]},"
						+ " JavaClassesAndSources {modWithTestRuntimeDeps-main-classes [modWithTestRuntimeDeps/src]},"
						+ " JavaClassesAndSources {modWithTestRuntimeDeps-test-classes [modWithTestRuntimeDeps/test]}]",
				report.classesToAnalyze().toString());
		assertEquals("[bin1]", report.auxClasses().toString());
	}

}
