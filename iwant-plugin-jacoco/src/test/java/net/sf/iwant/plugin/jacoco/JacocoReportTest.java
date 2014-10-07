package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.AsEmbeddedIwantUser;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;

public class JacocoReportTest extends IwantTestCase {

	@Override
	protected void moreSetUp() throws Exception {
		caches.cachesUrlAt(jacoco().zip().url(), cachedJacocoZip());
		jacoco().path(ctx);
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(jacoco().zip().url());
	}

	private Path asm() throws IOException {
		return downloaded(FromRepository.repo1MavenOrg().group("org/ow2/asm")
				.name("asm-all").version("5.0.1"));
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	private JavaClassesAndSources newJavaClassesAndSources(String name,
			String className, String... codeLinesForMain) throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);

		StringBuilder code = new StringBuilder();
		code.append("package " + name + ";\n");
		code.append("public class " + className + " {\n");
		code.append("  public static void main(String[] args) throws Throwable {\n");
		for (String codeLine : codeLinesForMain) {
			code.append(codeLine).append("\n");
		}
		code.append("  }\n");
		code.append("}\n");

		Iwant.newTextFile(new File(srcDir, className + ".java"),
				code.toString());
		JavaClasses classes = JavaClasses.with().name(name + "-classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		return new JavaClassesAndSources(classes,
				Source.underWsroot(srcDirString));
	}

	// the tests

	public void testIngredientsAndContentDescriptor() throws IOException {
		Path classes = Source.underWsroot("theclasses");
		Path sources = Source.underWsroot("thesources");
		JacocoCoverage coverage = JacocoCoverage.with().name("thecoverage")
				.jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar())
				.mainClassAndArguments("a.Test").classLocations(classes).end();
		JacocoReport report = JacocoReport.with().name("thereport")
				.jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).coverages(coverage)
				.classes(classes).sources(sources).end();

		assertEquals("[" + jacoco() + ", " + asm() + ", " + antJar() + ", "
				+ antLauncherJar() + ", thecoverage, theclasses, thesources]",
				report.ingredients().toString());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoReport\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n"
				+ "coverages:[thecoverage]\n" + "classes:[theclasses]\n"
				+ "sources:[thesources]\n" + "", report.contentDescriptor());
	}

	public void testReportWithNoncoveredAndPartlyCoveredModule()
			throws Exception {
		JavaClassesAndSources badTest = newJavaClassesAndSources("badtest",
				"BadTest", "System.err.println(\"This test covers nothing.\");");
		JavaClassesAndSources goodTest = newJavaClassesAndSources("goodtest",
				"GoodTest", "System.err.println(\"This test covers code.\");",
				"Class.forName(\"goodmain.GoodMain\").newInstance();");
		JavaClassesAndSources badMain = newJavaClassesAndSources("badmain",
				"BadMain", "System.err.println(\"This class is not covered\");");
		JavaClassesAndSources goodMain = newJavaClassesAndSources("goodmain",
				"GoodMain",
				"System.err.println(\"This class is covered (some)\");");
		JacocoInstrumentation badInstr = JacocoInstrumentation
				.of(badMain.classes()).using(jacoco()).with(asm());
		badInstr.path(ctx);
		JacocoInstrumentation goodInstr = JacocoInstrumentation
				.of(goodMain.classes()).using(jacoco()).with(asm());
		goodInstr.path(ctx);

		JacocoCoverage badCoverage = JacocoCoverage.with().name("bad-coverage")
				.jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar())
				.mainClassAndArguments("badtest.BadTest")
				.classLocations(badTest.classes(), badInstr).end();
		badCoverage.path(ctx);

		JacocoCoverage goodCoverage = JacocoCoverage.with()
				.name("util-coverage").jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar())
				.mainClassAndArguments("goodtest.GoodTest")
				.classLocations(goodTest.classes(), goodInstr).end();
		goodCoverage.path(ctx);

		JacocoReport report = JacocoReport.with().name("report")
				.jacocoWithDeps(jacoco(), asm())
				.coverages(badCoverage, goodCoverage)
				.classes(badMain.classes(), goodMain.classes())
				.sources(badMain.sources()).sources(goodMain.sources()).end();
		report.path(ctx);

		File reportDir = ctx.cached(report);

		assertTrue(contentOf(new File(reportDir, "index.html")).length() > 0);
		assertTrue(contentOf(new File(reportDir, "report.xml")).length() > 0);

		File coverageCsv = new File(reportDir, "report.csv");
		assertTrue(coverageCsv.exists());

		assertEquals(
				"GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED\n"
						+ "report,badmain,BadMain,7,0,0,0,3,0,2,0,2,0\n"
						+ "report,goodmain,GoodMain,4,3,0,0,2,1,1,1,1,1\n" + "",
				contentOf(coverageCsv));
	}

}