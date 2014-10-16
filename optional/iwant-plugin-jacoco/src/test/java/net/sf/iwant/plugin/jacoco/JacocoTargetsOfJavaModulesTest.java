package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.core.ClassNameList;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.StringFilter;
import net.sf.iwant.api.model.Target;

public class JacocoTargetsOfJavaModulesTest extends JacocoTestBase {

	public void testTargetsFromOneMinimalTestlessModule() throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		JacocoInstrumentation instr = jacocoTargets
				.jacocoInstrumentationOf(mod);
		assertEquals("mod-main-classes.jacoco-instr", instr.name());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoInstrumentation\n"
				+ "jacoco:" + jacoco() + "\n" + "deps:[" + asm() + "]\n"
				+ "antJars:[" + antJar() + ", " + antLauncherJar() + "]\n"
				+ "classes:mod-main-classes\n" + "", instr.contentDescriptor());

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("coverage-report");
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoReport\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n" + "coverages:[]\n"
				+ "classes:[mod-main-classes]\n" + "sources:[mod/src]\n" + "",
				report.contentDescriptor());
	}

	public void testReportName() throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();
		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		assertEquals("name1", jacocoTargets.jacocoReport("name1").name());
		assertEquals("name2", jacocoTargets.jacocoReport("name2").name());
	}

	public void testTargetsFromOneMinimalCodelessModule() throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoReport\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n" + "coverages:[]\n"
				+ "classes:[]\n" + "sources:[]\n" + "",
				report.contentDescriptor());
	}

	public void testTargetsFromOneMinimalTestOnlyModule() throws IOException {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);
		assertEquals("mod.jacococoverage", coverage.name());
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoCoverage\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n"
				+ "classLocations:[mod-test-classes]\n"
				+ "mainClassName:org.junit.runner.JUnitCore\n"
				+ "mainClassArgs:null\n"
				+ "mainClassArgsFile:mod-test-class-names\n" + "",
				coverage.contentDescriptor());

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoReport\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n"
				+ "coverages:[mod.jacococoverage]\n" + "classes:[]\n"
				+ "sources:[]\n" + "", report.contentDescriptor());
	}

	public void testTargetsFromOneBinaryModule() throws IOException {
		JavaBinModule mod = JavaBinModule.providing(Source.underWsroot("lib"))
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("net.sf.iwant.plugin.jacoco.JacocoReport\n" + "jacoco:"
				+ jacoco() + "\n" + "deps:[" + asm() + "]\n" + "antJars:["
				+ antJar() + ", " + antLauncherJar() + "]\n" + "coverages:[]\n"
				+ "classes:[lib]\n" + "sources:[]\n" + "",
				report.contentDescriptor());
	}

	public void testCoverageArgsForJunitIsClassNameListUnlessOnlyOneTestDefined()
			throws IOException {
		JavaSrcModule testedByOneClass = JavaSrcModule.with()
				.name("testedByOneClass").testJava("test")
				.testedBy("custom.Test").end();
		StringFilter sf = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				throw new UnsupportedOperationException("not needed");
			}

			@Override
			public String toString() {
				return "just-a-filter";
			}
		};
		JavaSrcModule testedByClassnameFilter = JavaSrcModule.with()
				.name("testedByClassnameFilter").testJava("test").testedBy(sf)
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar())
				.modules(testedByOneClass, testedByClassnameFilter).end();

		JacocoCoverage coverageOfOneClass = jacocoTargets
				.jacocoCoverageOf(testedByOneClass);
		assertEquals("org.junit.runner.JUnitCore",
				coverageOfOneClass.mainClassName());
		assertEquals("[custom.Test]", coverageOfOneClass.mainClassArgs()
				.toString());
		assertNull(coverageOfOneClass.mainClassArgsFile());

		JacocoCoverage coverageOfManyClasses = jacocoTargets
				.jacocoCoverageOf(testedByClassnameFilter);
		assertEquals("org.junit.runner.JUnitCore",
				coverageOfManyClasses.mainClassName());
		assertNull(coverageOfManyClasses.mainClassArgs());
		ClassNameList testNames = (ClassNameList) coverageOfManyClasses
				.mainClassArgsFile();
		assertEquals("net.sf.iwant.api.core.ClassNameList {\n"
				+ "  classes:testedByClassnameFilter-test-classes\n"
				+ "  filter:just-a-filter\n" + "}\n" + "",
				testNames.contentDescriptor());
	}

	public void testCoverageOfJavaSrcModuleWithCumulativeDeps()
			throws IOException {
		JavaBinModule bin1 = JavaBinModule
				.providing(Source.underWsroot("bin1")).end();
		JavaSrcModule src1 = JavaSrcModule.with().name("src1").mainJava("src")
				.mainDeps(bin1).end();
		JavaSrcModule src2 = JavaSrcModule.with().name("src2").mainJava("src")
				.end();
		JavaBinModule bin2 = JavaBinModule
				.providing(Source.underWsroot("bin2")).end();
		JavaBinModule testLib = JavaBinModule.providing(
				Source.underWsroot("testLib")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(src1, bin2, src2).testDeps(testLib)
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar()).modules(mod).end();

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);

		assertEquals("mod.jacococoverage", coverage.name());
		assertEquals("[mod-test-classes, testLib,"
				+ " mod-main-classes.jacoco-instr,"
				+ " src1-main-classes.jacoco-instr, bin1, bin2,"
				+ " src2-main-classes.jacoco-instr]", coverage.classLocations()
				.toString());
	}

	public void testCoverageOfSubsetOfSrcModulesWithDepToModuleOutsideTheSubset()
			throws Exception {
		JavaSrcModule uninteresting = JavaSrcModule.with()
				.name("uninteresting").mainJava("src").end();
		JavaSrcModule interesting1 = JavaSrcModule.with().name("interesting1")
				.mainJava("src").testJava("test").mainDeps(uninteresting)
				.testDeps(junit()).end();
		JavaSrcModule interesting2 = JavaSrcModule.with().name("interesting2")
				.mainJava("src").testJava("test").testDeps(junit()).end();

		wsRootHasFile("uninteresting/src/uninteresting/Uninteresting.java",
				"package uninteresting;\npublic class Uninteresting {"
						+ "public static int value() {return 1;}}\n");

		wsRootHasFile(
				"interesting1/src/interesting1/Interesting1.java",
				"package interesting1;\npublic class Interesting1 {"
						+ "public static int value() {return 2 + uninteresting.Uninteresting.value();}}\n");
		wsRootHasFile(
				"interesting1/test/interesting1/Interesting1Test.java",
				"package interesting1;import org.junit.Test;\nimport static org.junit.Assert.*;\n"
						+ "\npublic class Interesting1Test {"
						+ "@Test public void test() {assertEquals(3, Interesting1.value());}}\n");

		wsRootHasFile("interesting2/src/interesting2/Interesting2.java",
				"package interesting2;\npublic class Interesting2 {"
						+ "public static int value() {return 4;}}\n");
		wsRootHasFile(
				"interesting2/test/interesting2/Interesting2Test.java",
				"package interesting2;import org.junit.Test;\nimport static org.junit.Assert.*;\n"
						+ "\npublic class Interesting2Test {"
						+ "@Test public void test() {assertEquals(4, Interesting2.value());}}\n");

		((Target) uninteresting.mainArtifact()).path(ctx);
		((Target) interesting1.mainArtifact()).path(ctx);
		((Target) interesting1.testArtifact()).path(ctx);
		((Target) interesting2.mainArtifact()).path(ctx);
		((Target) interesting2.testArtifact()).path(ctx);

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacocoWithDeps(jacoco(), asm())
				.antJars(antJar(), antLauncherJar())
				.modules(interesting1, interesting2).end();

		JacocoReport report = jacocoTargets.jacocoReport("report");
		report.path(ctx);

		// uninteresting not included:
		assertEquals(
				"GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED\n"
						+ "report,interesting2,Interesting2,5,0,0,0,1,0,2,0,2,0\n"
						+ "report,interesting1,Interesting1,7,0,0,0,1,0,2,0,2,0\n"
						+ "", contentOf(new File(ctx.cached(report),
						"report.csv")));
	}

}
