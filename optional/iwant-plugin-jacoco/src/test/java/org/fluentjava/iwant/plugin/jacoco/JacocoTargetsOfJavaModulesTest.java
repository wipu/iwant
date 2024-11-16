package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;

import org.fluentjava.iwant.api.core.ClassNameList;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.TestRunner;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.api.model.SystemEnv;
import org.fluentjava.iwant.api.model.Target;

public class JacocoTargetsOfJavaModulesTest extends JacocoTestBase {

	public void testTargetsFromOneMinimalTestlessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		JacocoInstrumentation instr = jacocoTargets
				.jacocoInstrumentationOf(mod);
		assertEquals("mod-main-classes.jacoco-instr", instr.name());
		assertEquals(
				"org.fluentjava.iwant.plugin.jacoco.JacocoInstrumentation\n"
						+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n"
						+ "  " + antJar() + "\n  " + antLauncherJar()
						+ "\ni:classes:\n" + "  mod-main-classes\n" + "",
				instr.contentDescriptor());

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("coverage-report");
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoReport\n"
				+ "i:jacoco:\n" + "  " + jacoco() + "\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n" + "  " + antLauncherJar() + "\n"
				+ "i:coverages:\n" + "i:classes:\n" + "  mod-main-classes\n"
				+ "i:sources:\n" + "  mod/src\n" + "",
				report.contentDescriptor());
	}

	public void testReportName() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();
		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		assertEquals("name1", jacocoTargets.jacocoReport("name1").name());
		assertEquals("name2", jacocoTargets.jacocoReport("name2").name());
	}

	public void testTargetsFromOneMinimalCodelessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoReport\n"
				+ "i:jacoco:\n" + "  " + jacoco() + "\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n" + "  " + antLauncherJar() + "\n"
				+ "i:coverages:\n" + "i:classes:\n" + "i:sources:\n" + "",
				report.contentDescriptor());
	}

	public void testTargetsFromOneMinimalTestOnlyModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);
		assertEquals("mod.jacococoverage", coverage.name());
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoCoverage\n"
				+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n  " + antLauncherJar() + "\ni:classLocations:\n"
				+ "  mod-test-classes\n" + "p:mainClassName:\n"
				+ "  org.fluentjava.iwant.plugin.junit5runner.Junit5Runner\n"
				+ "p:mainClassArgs:\n" + " null-collection\n"
				+ "i:mainClassArgsFile:\n" + "  mod-test-class-names\n"
				+ "p:jvmargs:\n", coverage.contentDescriptor());

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoReport\n"
				+ "i:jacoco:\n" + "  " + jacoco() + "\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n" + "  " + antLauncherJar() + "\n"
				+ "i:coverages:\n" + "  mod.jacococoverage\n" + "i:classes:\n"
				+ "i:sources:\n" + "", report.contentDescriptor());
	}

	public void testTargetsFromOneBinaryModule() {
		JavaBinModule mod = JavaBinModule.providing(Source.underWsroot("lib"))
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		assertNull(jacocoTargets.jacocoInstrumentationOf(mod));

		assertNull(jacocoTargets.jacocoCoverageOf(mod));

		JacocoReport report = jacocoTargets.jacocoReport("report");
		assertEquals("org.fluentjava.iwant.plugin.jacoco.JacocoReport\n"
				+ "i:jacoco:\n" + "  jacoco-0.8.10\n" + "i:antJars:\n" + "  "
				+ antJar() + "\n  " + antLauncherJar() + "\ni:coverages:\n"
				+ "i:classes:\n" + "  lib\n" + "i:sources:\n" + "",
				report.contentDescriptor());
	}

	public void testCoverageArgsForJunitIsClassNameListUnlessOnlyOneTestDefined() {
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
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(testedByOneClass, testedByClassnameFilter).end();

		JacocoCoverage coverageOfOneClass = jacocoTargets
				.jacocoCoverageOf(testedByOneClass);
		assertEquals("org.fluentjava.iwant.plugin.junit5runner.Junit5Runner",
				coverageOfOneClass.mainClassName());
		assertEquals("[custom.Test]",
				coverageOfOneClass.mainClassArgs().toString());
		assertNull(coverageOfOneClass.mainClassArgsFile());

		JacocoCoverage coverageOfManyClasses = jacocoTargets
				.jacocoCoverageOf(testedByClassnameFilter);
		assertEquals("org.fluentjava.iwant.plugin.junit5runner.Junit5Runner",
				coverageOfManyClasses.mainClassName());
		assertNull(coverageOfManyClasses.mainClassArgs());
		ClassNameList testNames = (ClassNameList) coverageOfManyClasses
				.mainClassArgsFile();
		assertEquals(
				"org.fluentjava.iwant.api.core.ClassNameList\n" + "i:classes:\n"
						+ "  testedByClassnameFilter-test-classes\n"
						+ "p:filter:\n" + "  just-a-filter\n" + "",
				testNames.contentDescriptor());
	}

	public void testCoverageOfJavaSrcModuleWithCumulativeDeps() {
		JavaBinModule bin1 = JavaBinModule.providing(Source.underWsroot("bin1"))
				.end();
		JavaSrcModule src1 = JavaSrcModule.with().name("src1").mainJava("src")
				.mainDeps(bin1).end();
		JavaSrcModule src2 = JavaSrcModule.with().name("src2").mainJava("src")
				.end();
		JavaBinModule bin2 = JavaBinModule.providing(Source.underWsroot("bin2"))
				.end();
		JavaBinModule testLib = JavaBinModule
				.providing(Source.underWsroot("testLib")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(src1, bin2, src2).testDeps(testLib)
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);

		assertEquals("mod.jacococoverage", coverage.name());
		assertEquals(
				"[mod-test-classes, testLib,"
						+ " mod-main-classes.jacoco-instr,"
						+ " src1-main-classes.jacoco-instr, bin1, bin2,"
						+ " src2-main-classes.jacoco-instr]",
				coverage.classLocations().toString());
	}

	public void testCoverageOfSubsetOfSrcModulesWithDepToModuleOutsideTheSubset()
			throws Exception {
		JavaSrcModule uninteresting = JavaSrcModule.with().name("uninteresting")
				.mainJava("src").end();
		JavaSrcModule interesting1 = JavaSrcModule.with().name("interesting1")
				.mainJava("src").testJava("test").mainDeps(uninteresting)
				.testDeps(junit()).end();
		JavaSrcModule interesting2 = JavaSrcModule.with().name("interesting2")
				.mainJava("src").testJava("test").testDeps(junit()).end();

		wsRootHasFile("uninteresting/src/uninteresting/Uninteresting.java",
				"package uninteresting;\npublic class Uninteresting {"
						+ "public static int value() {return 1;}}\n");

		wsRootHasFile("interesting1/src/interesting1/Interesting1.java",
				"package interesting1;\npublic class Interesting1 {"
						+ "public static int value() {return 2 + uninteresting.Uninteresting.value();}}\n");
		wsRootHasFile("interesting1/test/interesting1/Interesting1Test.java",
				"package interesting1;import org.junit.Test;\nimport static org.junit.Assert.*;\n"
						+ "\npublic class Interesting1Test {"
						+ "@Test public void test() {assertEquals(3, Interesting1.value());}}\n");

		wsRootHasFile("interesting2/src/interesting2/Interesting2.java",
				"package interesting2;\npublic class Interesting2 {"
						+ "public static int value() {return 4;}}\n");
		wsRootHasFile("interesting2/test/interesting2/Interesting2Test.java",
				"package interesting2;import org.junit.Test;\nimport static org.junit.Assert.*;\n"
						+ "\npublic class Interesting2Test {"
						+ "@Test public void test() {assertEquals(4, Interesting2.value());}}\n");

		((Target) uninteresting.mainArtifact()).path(ctx);
		((Target) interesting1.mainArtifact()).path(ctx);
		((Target) interesting1.testArtifact()).path(ctx);
		((Target) interesting2.mainArtifact()).path(ctx);
		((Target) interesting2.testArtifact()).path(ctx);

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(interesting1, interesting2).end();

		JacocoReport report = jacocoTargets.jacocoReport("report");
		report.path(ctx);

		// uninteresting not included:
		assertEquals(
				"GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED\n"
						+ "report,interesting1,Interesting1,7,0,0,0,1,0,2,0,2,0\n"
						+ "report,interesting2,Interesting2,5,0,0,0,1,0,2,0,2,0\n"
						+ "",
				contentOf(new File(ctx.cached(report), "report.csv")));
	}

	public void testJacocoCoverageUsesModulesTestEnv() {
		SystemEnv env = SystemEnv.with().string("a", "a1")
				.path("b", Source.underWsroot("b")).end();
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.testEnv(env).end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);

		assertSame(env, coverage.env());
	}

	public void testOwnJunitRunnerIsUsedForCoverageByDefault() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);
		assertEquals("org.fluentjava.iwant.plugin.junit5runner.Junit5Runner",
				coverage.mainClassName());
	}

	public void testCustomTestRunnerIsUsedForCoverage() {
		class CustomRunner implements TestRunner {
			@Override
			public String mainClassName() {
				return "custom.TestRunner";
			}
		}
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.testRunner(new CustomRunner()).end();

		JacocoTargetsOfJavaModules jacocoTargets = JacocoTargetsOfJavaModules
				.with().jacoco(jacoco()).antJars(antJar(), antLauncherJar())
				.modules(mod).end();

		JacocoCoverage coverage = jacocoTargets.jacocoCoverageOf(mod);
		assertEquals("custom.TestRunner", coverage.mainClassName());
	}

}
