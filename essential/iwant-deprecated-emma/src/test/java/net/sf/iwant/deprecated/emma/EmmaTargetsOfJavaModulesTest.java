package net.sf.iwant.deprecated.emma;

import junit.framework.TestCase;
import net.sf.iwant.api.core.ClassNameList;
import net.sf.iwant.api.javamodules.DefaultTestClassNameFilter;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.StringFilter;

public class EmmaTargetsOfJavaModulesTest extends TestCase {

	private Source emma;
	private Source ant;
	private Source antLauncher;

	@Override
	protected void setUp() throws Exception {
		emma = Source.underWsroot("mocked-emma");
		ant = Source.underWsroot("mocked-ant");
		antLauncher = Source.underWsroot("mocked-ant-launcher");
	}

	public void testTargetsFromOneMinimalTestlessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("mod-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, mod-main-classes]",
				instr.ingredients().toString());

		assertNull(emmaTargets.emmaCoverageOf(mod));

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("[mocked-emma, mod-main-classes.emma-instr]",
				report.ingredients().toString());
	}

	public void testEmmaReportName() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();
		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		assertEquals("name1", emmaTargets.emmaReport("name1").name());
		assertEquals("name2", emmaTargets.emmaReport("name2").name());
	}

	public void testTargetsFromOneMinimalCodelessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		assertNull(emmaTargets.emmaInstrumentationOf(mod));

		assertNull(emmaTargets.emmaCoverageOf(mod));

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("[mocked-emma]", report.ingredients().toString());
	}

	public void testTargetsFromOneMinimalTestOnlyModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").testJava("test")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		assertNull(emmaTargets.emmaInstrumentationOf(mod));

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals("net.sf.iwant.deprecated.emma.EmmaCoverage\n" + "i:emma:\n"
				+ "  mocked-emma\n" + "i:antJars:\n" + "  mocked-ant\n"
				+ "  mocked-ant-launcher\n" + "p:mainClass:\n"
				+ "  org.junit.runner.JUnitCore\n" + "p:mainClassArguments:\n"
				+ " null-collection\n" + "i:mainClassArgumentsFile:\n"
				+ "  mod-test-class-names\n" + "i:classpath:\n"
				+ "  mod-test-classes\n" + "p:jvmargs:\n"
				+ "  -XX:-UseSplitVerifier\n" + "  -Demma.rt.control=false\n"
				+ "", coverage.contentDescriptor());

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("[mocked-emma, mod.emmacoverage]",
				report.ingredients().toString());
	}

	public void testTargetsFromOneBinaryModule() {
		JavaBinModule mod = JavaBinModule.providing(Source.underWsroot("lib"))
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		assertNull(emmaTargets.emmaInstrumentationOf(mod));

		assertNull(emmaTargets.emmaCoverageOf(mod));

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("[mocked-emma]", report.ingredients().toString());
	}

	public void testFilteredInstrumentationOfMinimalModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher)
				.filter(Source.underWsroot("filter")).modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("[mocked-emma, mod-main-classes, filter]",
				instr.ingredients().toString());
	}

	public void testTargetsFromOneMinimalModuleWithTests() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("mod-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, mod-main-classes]",
				instr.ingredients().toString());

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals("net.sf.iwant.deprecated.emma.EmmaCoverage\n" + "i:emma:\n"
				+ "  mocked-emma\n" + "i:antJars:\n" + "  mocked-ant\n"
				+ "  mocked-ant-launcher\n" + "p:mainClass:\n"
				+ "  org.junit.runner.JUnitCore\n" + "p:mainClassArguments:\n"
				+ " null-collection\n" + "i:mainClassArgumentsFile:\n"
				+ "  mod-test-class-names\n" + "i:classpath:\n"
				+ "  mod-test-classes\n" + "  mod-main-classes.emma-instr\n"
				+ "p:jvmargs:\n" + "  -XX:-UseSplitVerifier\n"
				+ "  -Demma.rt.control=false\n" + "",
				coverage.contentDescriptor());

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("emma-coverage", report.name());
		assertEquals(
				"[mocked-emma, "
						+ "mod-main-classes.emma-instr, mod.emmacoverage]",
				report.ingredients().toString());
	}

	public void testCoveragesAndReportFromOneTestedAndOneUntestedModule() {
		JavaSrcModule tested = JavaSrcModule.with().name("tested")
				.mainJava("src").testJava("test").end();
		JavaSrcModule untested = JavaSrcModule.with().name("untested")
				.mainJava("src").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(tested, untested)
				.end();

		assertNotNull(emmaTargets.emmaCoverageOf(tested));
		assertNull(emmaTargets.emmaCoverageOf(untested));

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals("emma-coverage", report.name());
		assertEquals(
				"[mocked-emma, tested-main-classes.emma-instr,"
						+ " untested-main-classes.emma-instr, tested.emmacoverage]",
				report.ingredients().toString());
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

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);

		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals(
				"[mod-test-classes, testLib," + " mod-main-classes.emma-instr,"
						+ " src1-main-classes.emma-instr, bin1, bin2,"
						+ " src2-main-classes.emma-instr]",
				coverage.classPathIngredients().toString());
	}

	public void testMainClassAndArgumentsOfCoverageOfModuleWithTestSuiteName() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testedBy("org.oikarinen.TestSuite").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("org.junit.runner.JUnitCore", coverage.mainClass());
		assertNull(coverage.mainClassArgumentsFile());

		assertEquals("[org.oikarinen.TestSuite]",
				coverage.mainClassArguments().toString());
	}

	public void testMainClassAndArgumentsFileOfCoverageOfModuleWithNoTestClassDefinition() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("org.junit.runner.JUnitCore", coverage.mainClass());
		assertNull(coverage.mainClassArguments());

		ClassNameList arg = (ClassNameList) coverage.mainClassArgumentsFile();
		assertEquals("mod-test-class-names", arg.name());
		assertEquals("mod-test-classes", arg.classes().toString());
		assertTrue(arg.filter() instanceof DefaultTestClassNameFilter);
	}

	public void testMainClassAndArgumentsFileOfCoverageOfModuleWithClassDefinitionAsFilter() {
		StringFilter filter = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				return candidate.contains("Test");
			}
		};
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testedBy(filter).end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("org.junit.runner.JUnitCore", coverage.mainClass());
		assertNull(coverage.mainClassArguments());

		ClassNameList arg = (ClassNameList) coverage.mainClassArgumentsFile();
		assertEquals("mod-test-class-names", arg.name());
		assertEquals("mod-test-classes", arg.classes().toString());
		assertSame(filter, arg.filter());
	}

	public void testExcludingModuleFromInstrumentation() {
		JavaSrcModule toExclude = JavaSrcModule.with().name("toExclude")
				.mainJava("src").testJava("test").end();
		JavaSrcModule normal = JavaSrcModule.with().name("normal")
				.mainJava("src").testJava("test").mainDeps(toExclude).end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(normal, toExclude)
				.butNotInstrumenting(toExclude).end();

		assertNull(emmaTargets.emmaInstrumentationOf(toExclude));
		EmmaCoverage exclCoverage = emmaTargets.emmaCoverageOf(toExclude);
		assertEquals(
				"[mocked-emma, mocked-ant, mocked-ant-launcher, "
						+ "toExclude-test-class-names, toExclude-test-classes, "
						+ "toExclude-main-classes]",
				exclCoverage.ingredients().toString());

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(normal);
		assertEquals("normal-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, normal-main-classes]",
				instr.ingredients().toString());

		EmmaCoverage normalCoverage = emmaTargets.emmaCoverageOf(normal);
		assertEquals(
				"[mocked-emma, mocked-ant, mocked-ant-launcher, "
						+ "normal-test-class-names, normal-test-classes, "
						+ "normal-main-classes.emma-instr, "
						+ "toExclude-main-classes]",
				normalCoverage.ingredients().toString());
	}

	public void testTwoModulesWithCoverage() {
		JavaSrcModule mod1 = JavaSrcModule.with().name("mod1").mainJava("src")
				.testJava("test").end();
		JavaSrcModule mod2 = JavaSrcModule.with().name("mod2").mainJava("src")
				.testJava("test").mainDeps(mod1).end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod2, mod1).end();

		EmmaCoverage cov1 = emmaTargets.emmaCoverageOf(mod1);
		assertEquals(
				"[mocked-emma, mocked-ant, mocked-ant-launcher, "
						+ "mod1-test-class-names, mod1-test-classes, mod1-main-classes.emma-instr]",
				cov1.ingredients().toString());

		EmmaCoverage cov2 = emmaTargets.emmaCoverageOf(mod2);
		assertEquals(
				"[mocked-emma, mocked-ant, mocked-ant-launcher, "
						+ "mod2-test-class-names, mod2-test-classes, mod2-main-classes.emma-instr, "
						+ "mod1-main-classes.emma-instr]",
				cov2.ingredients().toString());

		EmmaReport report = emmaTargets.emmaReport("emma-coverage");
		assertEquals(
				"[mocked-emma, "
						+ "mod1-main-classes.emma-instr, mod2-main-classes.emma-instr, "
						+ "mod1.emmacoverage, mod2.emmacoverage]",
				report.ingredients().toString());
	}

	public void testEmmaCoverageDefaultJvmArgsContainsUseSplitVerifierSoJava17CanBeUsed() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant, antLauncher).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertTrue(coverage.jvmargs().contains("-XX:-UseSplitVerifier"));
	}

}
