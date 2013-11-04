package net.sf.iwant.plugin.findbugs;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.api.AsEmbeddedIwantUser;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

import org.apache.commons.io.FileUtils;

public class FindbugsReportTest extends TestCase {

	private IwantPluginFindbugsTestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginFindbugsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = new File(testArea.root(), "cached");
		caches.cachesModifiableTargetsAt(cached);
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
		testArea.newDir("empty-src");
		testArea.newDir("empty-classes");
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

		Target report = FindbugsReport
				.with()
				.name("fb-empty")
				.using(distroToTest(), antJar(), antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(emptyClasses, emptySrc))
				.end();

		assertEquals("[findbugs-2.0.2, " + antJar() + ", " + antLauncherJar()
				+ ", empty-classes, empty-src]", report.ingredients()
				.toString());
		assertEquals("net.sf.iwant.plugin.findbugs.FindbugsReport {\n"
				+ "  ingredients: {\n" + "    findbugs-2.0.2\n" + "    "
				+ antJar() + "\n" + "    " + antLauncherJar() + "\n"
				+ "    empty-classes\n" + "    empty-src\n" + "  }\n"
				+ "  classesToAnalyze: {\n"
				+ "    JavaClassesAndSources {empty-classes [empty-src]}\n"
				+ "  }\n" + "}\n" + "", report.contentDescriptor().toString());
	}

	public void testDefaultReportFromEmptyClasses() throws Exception {
		testArea.newDir("empty-src");
		testArea.newDir("empty-classes");
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

}
