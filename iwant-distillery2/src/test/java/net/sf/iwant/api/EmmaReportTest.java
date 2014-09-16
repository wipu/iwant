package net.sf.iwant.api;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;

public class EmmaReportTest extends IwantTestCase {

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	private Path emma() throws IOException {
		return downloaded(TestedIwantDependencies.emma());
	}

	private Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	private Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	private Path junit() throws IOException {
		return downloaded(TestedIwantDependencies.junit());
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

	public void testIngredientsAndDescriptor() throws Exception {
		JavaClassesAndSources classesAndSources = new JavaClassesAndSources(
				Source.underWsroot("classes"), Source.underWsroot("src"));
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("Whatever").instrumentations(instr)
				.end();

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages(coverage).end();

		assertEquals("[" + emma()
				+ ", classes.emma-instr, instrtest-emma-coverage]", report
				.ingredients().toString());
		assertEquals("net.sf.iwant.api.EmmaReport:[" + emma()
				+ ", classes.emma-instr, instrtest-emma-coverage]",
				report.contentDescriptor());

	}

	public void testNonInstrumentedDependency() throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "JunitReferrer",
				"System.err.println(\"found \"+Class.forName(\"org.junit.runner.JUnitCore\"));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("instrtest.JunitReferrer")
				.instrumentations(instr).nonInstrumentedClasses(junit()).end();
		coverage.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages(coverage).end();
		report.path(ctx);

		assertTrue(new File(ctx.cached(report), "coverage.txt").exists());
		assertTrue(new File(ctx.cached(report), "coverage/index.html").exists());
		assertTrue(new File(ctx.cached(report), "coverage.xml").exists());
	}

	public void testFilteredOutModuleDoesNotBreakReportBuilding()
			throws Exception {
		String filterFileString = "emma-filter.txt";
		Iwant.newTextFile(new File(wsRoot, filterFileString), "-Filtered\n");
		Path filter = Source.underWsroot(filterFileString);

		JavaClassesAndSources cs1 = newJavaClassesAndSources("filtered",
				"Filtered");
		EmmaInstrumentation instr1 = EmmaInstrumentation.of(cs1).filter(filter)
				.using(emma());
		instr1.path(ctx);

		JavaClassesAndSources cs2 = newJavaClassesAndSources("nonfiltered",
				"NonFiltered");
		EmmaInstrumentation instr2 = EmmaInstrumentation.of(cs2).filter(filter)
				.using(emma());
		instr2.path(ctx);

		EmmaCoverage coverage = EmmaCoverage.with()
				.name("instrtest-emma-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("nonfiltered.NonFiltered")
				.instrumentations(instr1, instr2).end();
		coverage.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr1, instr2).coverages(coverage).end();
		report.path(ctx);

		assertTrue(new File(ctx.cached(report), "coverage.txt").exists());
		assertTrue(new File(ctx.cached(report), "coverage/index.html").exists());
		assertTrue(new File(ctx.cached(report), "coverage.xml").exists());
	}

	public void testEmptyReportDirectoryIsGeneratedWhenNoCoveragesAreGiven()
			throws Exception {
		JavaClassesAndSources classesAndSources = newJavaClassesAndSources(
				"instrtest", "JunitReferrer",
				"System.err.println(\"found \"+Class.forName(\"org.junit.runner.JUnitCore\"));");
		EmmaInstrumentation instr = EmmaInstrumentation.of(classesAndSources)
				.using(emma());
		instr.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(instr).coverages().end();
		report.path(ctx);

		assertTrue(ctx.cached(report).exists());
		assertFalse(new File(ctx.cached(report), "coverage.txt").exists());
		assertFalse(new File(ctx.cached(report), "coverage/index.html")
				.exists());
		assertFalse(new File(ctx.cached(report), "coverage.xml").exists());
	}

	public void testMissingEcCausedByZeroCoverageIsIgnoredAndEmmaReportReportsZeroCoverage()
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
		EmmaInstrumentation badInstr = EmmaInstrumentation.of(badMain).using(
				emma());
		badInstr.path(ctx);
		EmmaInstrumentation goodInstr = EmmaInstrumentation.of(goodMain).using(
				emma());
		goodInstr.path(ctx);

		EmmaCoverage badCoverage = EmmaCoverage.with().name("bad-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("badtest.BadTest")
				.instrumentations(badInstr)
				.nonInstrumentedClasses(badTest.classes()).end();
		badCoverage.path(ctx);

		EmmaCoverage goodCoverage = EmmaCoverage.with().name("good-coverage")
				.antJars(antJar(), antLauncherJar()).emma(emma())
				.mainClassAndArguments("goodtest.GoodTest")
				.instrumentations(goodInstr)
				.nonInstrumentedClasses(goodTest.classes()).end();
		goodCoverage.path(ctx);

		EmmaReport report = EmmaReport.with().name("report").emma(emma())
				.instrumentations(badInstr, goodInstr)
				.coverages(badCoverage, goodCoverage).end();
		report.path(ctx);

		File coverageTxt = new File(ctx.cached(report), "coverage.txt");
		assertTrue(coverageTxt.exists());

		assertTrue(contentOf(coverageTxt).contains(
				"0%   (0/1)!	0%   (0/2)!	0%   (0/7)!	0%   (0/3)!	badmain"));
		assertTrue(contentOf(coverageTxt).contains(
				"100% (1/1)	50%  (1/2)!	43%  (3/7)!	33%  (1/3)!	goodmain"));
	}

}
