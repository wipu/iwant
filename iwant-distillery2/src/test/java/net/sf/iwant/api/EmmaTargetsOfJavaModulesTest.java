package net.sf.iwant.api;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;

public class EmmaTargetsOfJavaModulesTest extends TestCase {

	private Source emma;
	private Source ant;

	@Override
	protected void setUp() throws Exception {
		emma = Source.underWsroot("mocked-emma");
		ant = Source.underWsroot("mocked-ant");
	}

	public void testTargetsFromOneMinimalTestlessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("mod-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, mod-main-classes]", instr.ingredients()
				.toString());

		assertNull(emmaTargets.emmaCoverageOf(mod));

		assertNull(emmaTargets.emmaReport());
	}

	public void testTargetsFromOneMinimalCodelessModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		assertNull(emmaTargets.emmaInstrumentationOf(mod));

		assertNull(emmaTargets.emmaCoverageOf(mod));

		assertNull(emmaTargets.emmaReport());
	}

	public void testTargetsFromOneBinaryModule() {
		JavaBinModule mod = JavaBinModule.providing(Source.underWsroot("lib"));

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		assertNull(emmaTargets.emmaInstrumentationOf(mod));

		assertNull(emmaTargets.emmaCoverageOf(mod));

		assertNull(emmaTargets.emmaReport());
	}

	public void testFilteredInstrumentationOfMinimalModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).filter(Source.underWsroot("filter"))
				.modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("[mocked-emma, mod-main-classes, filter]", instr
				.ingredients().toString());
	}

	public void testTargetsFromOneMinimalModuleWithTests() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(mod);
		assertEquals("mod-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, mod-main-classes]", instr.ingredients()
				.toString());

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals("[mocked-ant, mocked-emma, "
				+ "mod-test-classes, mod-main-classes.emma-instr, "
				+ "mod-test-class-names]", coverage.ingredients().toString());

		EmmaReport report = emmaTargets.emmaReport();
		assertEquals("emma-coverage", report.name());
		assertEquals("[mocked-emma, "
				+ "mod-main-classes.emma-instr, mod.emmacoverage]", report
				.ingredients().toString());
	}

	public void testCoverageOfJavaSrcModuleWithCumulativeDeps() {
		JavaBinModule bin1 = JavaBinModule
				.providing(Source.underWsroot("bin1"));
		JavaSrcModule src1 = JavaSrcModule.with().name("src1").mainJava("src")
				.mainDeps(bin1).end();
		JavaSrcModule src2 = JavaSrcModule.with().name("src2").mainJava("src")
				.end();
		JavaBinModule bin2 = JavaBinModule
				.providing(Source.underWsroot("bin2"));
		JavaBinModule testLib = JavaBinModule.providing(Source
				.underWsroot("testLib"));

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(src1, bin2, src2).testDeps(testLib)
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);

		assertEquals("mod.emmacoverage", coverage.name());
		assertEquals("[mod-test-classes, testLib,"
				+ " mod-main-classes.emma-instr,"
				+ " src1-main-classes.emma-instr, bin1, bin2,"
				+ " src2-main-classes.emma-instr]", coverage
				.classPathIngredients().toString());
	}

	public void testMainClassAndArgumentsOfCoverageOfModuleWithTestSuiteName() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testSuiteName("org.oikarinen.TestSuite")
				.end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("org.junit.runner.JUnitCore", coverage.mainClass());
		assertNull(coverage.mainClassArgumentsFile());

		assertEquals("[org.oikarinen.TestSuite]", coverage.mainClassArguments()
				.toString());
	}

	public void testMainClassAndArgumentsFileOfCoverageOfModuleWithNoTestSuiteName() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(mod).end();

		EmmaCoverage coverage = emmaTargets.emmaCoverageOf(mod);
		assertEquals("org.junit.runner.JUnitCore", coverage.mainClass());
		assertNull(coverage.mainClassArguments());

		ClassNameList arg = (ClassNameList) coverage.mainClassArgumentsFile();
		assertEquals("mod-test-class-names", arg.name());
		assertEquals("mod-test-classes", arg.classes().toString());

	}

	public void testExcludingModuleFromInstrumentation() {
		JavaSrcModule toExclude = JavaSrcModule.with().name("toExclude")
				.mainJava("src").testJava("test").end();
		JavaSrcModule normal = JavaSrcModule.with().name("normal")
				.mainJava("src").testJava("test").mainDeps(toExclude).end();

		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.emma(emma).antJars(ant).modules(normal, toExclude)
				.butNotInstrumenting(toExclude).end();

		assertNull(emmaTargets.emmaInstrumentationOf(toExclude));
		EmmaCoverage exclCoverage = emmaTargets.emmaCoverageOf(toExclude);
		assertEquals("[mocked-ant, mocked-emma, toExclude-test-classes, "
				+ "toExclude-main-classes, toExclude-test-class-names]",
				exclCoverage.ingredients().toString());

		EmmaInstrumentation instr = emmaTargets.emmaInstrumentationOf(normal);
		assertEquals("normal-main-classes.emma-instr", instr.name());
		assertEquals("[mocked-emma, normal-main-classes]", instr.ingredients()
				.toString());

		EmmaCoverage normalCoverage = emmaTargets.emmaCoverageOf(normal);
		assertEquals("[mocked-ant, mocked-emma, normal-test-classes, "
				+ "normal-main-classes.emma-instr, "
				+ "toExclude-main-classes, normal-test-class-names]",
				normalCoverage.ingredients().toString());
	}

}
