package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.BuildUtility;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionCode;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionConfiguration;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionRuntimeData;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.TestCode;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.TestRuntimeData;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.TestUtility;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.api.model.Target;
import org.junit.jupiter.api.Test;

public class JavaSrcModuleTest {

	@Test
	public void toStringIsTheName() {
		assertEquals("m1", JavaSrcModule.with().name("m1").end().toString());
		assertEquals("m2", JavaSrcModule.with().name("m2").end().toString());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void equalsUsesNameAndClass() {
		assertTrue(JavaSrcModule.with().name("a").end()
				.equals(JavaSrcModule.with().name("a").end()));
		assertTrue(JavaSrcModule.with().name("a").end()
				.equals(JavaSrcModule.with().name("a").mainJava("src").end()));

		assertFalse(JavaSrcModule.with().name("a").end().equals(
				JavaBinModule.providing(Source.underWsroot("a")).end()));
		assertFalse(JavaSrcModule.with().name("a").end()
				.equals(JavaSrcModule.with().name("b").end()));
	}

	@Test
	public void hashCodeIsSameIfNameIsSame() {
		assertTrue(JavaSrcModule.with().name("a").end()
				.hashCode() == JavaSrcModule.with().name("a").end().hashCode());
		assertTrue(
				JavaSrcModule.with().name("a").end().hashCode() == JavaBinModule
						.providing(Source.underWsroot("a")).end().hashCode());
	}

	@Test
	public void parentDirectoryNormalizationAffectsLocationUnderWsRoot() {
		assertEquals("a",
				JavaSrcModule.with().name("a").end().locationUnderWsRoot());
		assertEquals("a", JavaSrcModule.with().name("a").relativeParentDir("")
				.end().locationUnderWsRoot());
		assertEquals("dir/a", JavaSrcModule.with().name("a")
				.relativeParentDir("dir").end().locationUnderWsRoot());
		assertEquals("dir/a", JavaSrcModule.with().name("a")
				.relativeParentDir("dir/").end().locationUnderWsRoot());
	}

	@Test
	public void parentDirectoryNormalizationAffectsWsrootRelativeParentDir() {
		assertEquals("",
				JavaSrcModule.with().name("a").end().wsrootRelativeParentDir());
		assertEquals("", JavaSrcModule.with().name("a").relativeParentDir("")
				.end().wsrootRelativeParentDir());
		assertEquals("dir", JavaSrcModule.with().name("a")
				.relativeParentDir("dir").end().wsrootRelativeParentDir());
		assertEquals("dir", JavaSrcModule.with().name("a")
				.relativeParentDir("dir/").end().wsrootRelativeParentDir());

		assertEquals("dir1/dir2",
				JavaSrcModule.with().name("proj-name")
						.locationUnderWsRoot("dir1/dir2/proj").end()
						.wsrootRelativeParentDir());
	}

	@Test
	public void relativeWsRoot() {
		assertEquals("..",
				JavaSrcModule.with().name("a").end().relativeWsRoot());
		assertEquals("../..", JavaSrcModule.with().name("a")
				.relativeParentDir("one").end().relativeWsRoot());
		assertEquals("../../..", JavaSrcModule.with().name("a")
				.relativeParentDir("one/two").end().relativeWsRoot());
	}

	@Test
	public void locationUnderWsRootFromParentAndName() {
		assertEquals("simple", JavaSrcModule.with().name("simple").end()
				.locationUnderWsRoot());
		assertEquals("one/nested", JavaSrcModule.with().name("nested")
				.relativeParentDir("one").end().locationUnderWsRoot());
		assertEquals("one/two/nested", JavaSrcModule.with().name("nested")
				.relativeParentDir("one/two").end().locationUnderWsRoot());
	}

	@Test
	public void locationThatDiffersFromName() {
		JavaSrcModule module = JavaSrcModule.with().name("module-name")
				.locationUnderWsRoot("parent/dir-name").end();

		assertEquals("../..", module.relativeWsRoot());
		assertEquals("parent/dir-name", module.locationUnderWsRoot());
	}

	@Test
	public void settingBothParentAndLocationIsAnError() {
		IwantSrcModuleSpex spex = JavaSrcModule.with().name("module-name")
				.relativeParentDir("parent")
				.locationUnderWsRoot("parent/dir-name");
		try {
			spex.end();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(
					"You must not specify both relativeParentDir and locationUnderWsRoot",
					e.getMessage());
		}
	}

	// java classes

	@Test
	public void mainJavasAsPathsFromSourcelessSrcModule() {
		assertTrue(JavaSrcModule.with().name("srcless").end().mainJavasAsPaths()
				.isEmpty());
	}

	@Test
	public void mainJavasAsPathsFromNormalSrcModule() {
		assertEquals("[simple/src]", JavaSrcModule.with().name("simple")
				.mainJava("src").end().mainJavasAsPaths().toString());
		assertEquals("[parent/more-nesting/src/main/java]",
				JavaSrcModule.with().name("more-nesting")
						.relativeParentDir("parent").mainJava("src/main/java")
						.end().mainJavasAsPaths().toString());
	}

	@Test
	public void mainJavasAsPathsFromCodeGenerationModule() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();

		List<Path> mainJavas = module.mainJavasAsPaths();
		assertEquals(1, mainJavas.size());
		assertSame(generatedSrc, mainJavas.get(0));
	}

	@Test
	public void testJavaAsPathWithNullAndNonNull() {
		assertTrue(JavaSrcModule.with().name("testless").end()
				.testJavasAsPaths().isEmpty());
		assertEquals("[tested/test]", JavaSrcModule.with().name("tested")
				.testJava("test").end().testJavasAsPaths().toString());
	}

	@Test
	public void sourcelessSrcModuleHasNoMainArtifact() {
		JavaSrcModule module = JavaSrcModule.with().name("srcless").end();

		assertNull(module.mainArtifact());
	}

	@Test
	public void mainArtifactOfCodeGenerationModule() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.nativePathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();

		Target mainArtifact = (Target) module.mainArtifact();
		assertSame(generatedClasses, mainArtifact);
	}

	@Test
	public void mainArtifactOfMinimalSrcModuleIsJavaClassesCompiledFromSource() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("[simple/src]", mainArtifact.srcDirs().toString());
		assertEquals("[]", mainArtifact.classLocations().toString());
	}

	@Test
	public void mainArtifactOfOfSrcModuleThatHasDependencies() {
		JavaSrcModule util1 = JavaSrcModule.with().name("util1")
				.mainJava("src/main/java").end();
		JavaSrcModule util2 = JavaSrcModule.with().name("util2")
				.mainJava("src/main/java").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src/main/java").mainDeps(util1, util2).end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("[simple/src/main/java]",
				mainArtifact.srcDirs().toString());
		assertEquals("[util1-main-classes, util2-main-classes]",
				mainArtifact.classLocations().toString());
	}

	@Test
	public void mainArtifactOfOfSrcModuleThatHasManyMainJavas() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-src")
				.mainJava("src1").mainJava("src2").mainDeps().end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("[dual-src/src1, dual-src/src2]",
				mainArtifact.srcDirs().toString());
	}

	@Test
	public void mainJavaCollectionCanBeEmptiedDuringSpecification() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-src")
				.mainJava("src1").noMainJava().mainJava("src2").mainDeps()
				.end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("[dual-src/src2]", mainArtifact.srcDirs().toString());
	}

	@Test
	public void mainResourcesCollectionCanBeEmptiedDuringSpecification() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-res")
				.mainResources("res1").noMainResources().mainResources("res2")
				.mainDeps().end();

		assertEquals("[res2]", module.mainResources().toString());
	}

	@Test
	public void testArtifactOfOfSrcModuleThatHasManyTestJavas() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-test")
				.testJava("test1").testJava("test2").mainDeps().end();

		JavaClasses testArtifact = (JavaClasses) module.testArtifact();

		assertEquals("[dual-test/test1, dual-test/test2]",
				testArtifact.srcDirs().toString());
	}

	@Test
	public void testJavaCollectionCanBeEmptiedDuringSpecification() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-test")
				.testJava("test1").noTestJava().testJava("test2").mainDeps()
				.end();

		JavaClasses testArtifact = (JavaClasses) module.testArtifact();

		assertEquals("[dual-test/test2]", testArtifact.srcDirs().toString());
	}

	@Test
	public void testResourcesCollectionCanBeEmptiedDuringSpecification() {
		JavaSrcModule module = JavaSrcModule.with().name("dual-res")
				.testResources("res1").noTestResources().testResources("res2")
				.mainDeps().end();

		assertEquals("[res2]", module.testResources().toString());
	}

	@Test
	public void mainResourcesArePassedToMainClassesWhileTestResourcesAreMissing() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.mainResources("res").testJava("test").end();

		JavaClasses mainClasses = (JavaClasses) mod.mainArtifact();
		assertEquals("[mod/res]", mainClasses.resourceDirs().toString());
		JavaClasses testClasses = (JavaClasses) mod.testArtifact();
		assertEquals("[]", testClasses.resourceDirs().toString());

		assertEquals("[mod/res]", mod.mainResourcesAsPaths().toString());
		assertEquals("[]", mod.testResourcesAsPaths().toString());
	}

	@Test
	public void testResourcesArePassedToTestClassesWhileMainResourcesAreMissing() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testResources("test-res").end();

		JavaClasses mainClasses = (JavaClasses) mod.mainArtifact();
		assertEquals("[]", mainClasses.resourceDirs().toString());
		JavaClasses testClasses = (JavaClasses) mod.testArtifact();
		assertEquals("[mod/test-res]", testClasses.resourceDirs().toString());

		assertEquals("[]", mod.mainResourcesAsPaths().toString());
		assertEquals("[mod/test-res]", mod.testResourcesAsPaths().toString());
	}

	@Test
	public void modulesAreComparedByName() {
		JavaModule srcA = JavaSrcModule.with().name("a").end();
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule binA = JavaBinModule.named("a").inside(libs).end();

		assertTrue(srcA.compareTo(binA) == 0);
		assertTrue(binA.compareTo(srcA) == 0);

		assertTrue(srcA.compareTo(libs) < 0);
		assertTrue(binA.compareTo(libs) < 0);

		assertTrue(libs.compareTo(srcA) > 0);
		assertTrue(libs.compareTo(binA) > 0);
	}

	@Test
	public void testArtifactIsNullWhenNoTests() {
		assertNull(JavaSrcModule.with().name("testless").mainJava("src").end()
				.testArtifact());
	}

	@Test
	public void testArtifactIsWhenThereAreTests() {
		JavaClasses tests = (JavaClasses) JavaSrcModule.with().name("tested")
				.mainJava("src").testJava("test").end().testArtifact();

		assertEquals("tested-test-classes", tests.name());
		assertEquals("[tested/test]", tests.srcDirs().toString());
		assertEquals("[tested-main-classes]",
				tests.classLocations().toString());
	}

	@Test
	public void testArtifactWithMainAndTestDeps() {
		JavaClasses tests = (JavaClasses) JavaSrcModule.with().name("tested2")
				.mainJava("src")
				.mainDeps(JavaBinModule
						.providing(Source.underWsroot("main-lib.jar"), null)
						.end())
				.testJava("test")
				.testDeps(JavaBinModule
						.providing(Source.underWsroot("test-lib.jar"), null)
						.end())
				.end().testArtifact();

		assertEquals("tested2-test-classes", tests.name());
		assertEquals("[tested2/test]", tests.srcDirs().toString());
		assertEquals("[tested2-main-classes, main-lib.jar, test-lib.jar]",
				tests.classLocations().toString());
	}

	@Test
	public void testArtifactWhenNoMainJava() {
		JavaClasses tests = (JavaClasses) JavaSrcModule.with()
				.name("only-tests").testJava("test").end().testArtifact();

		assertEquals("only-tests-test-classes", tests.name());
		assertEquals("[only-tests/test]", tests.srcDirs().toString());
		assertEquals("[]", tests.classLocations().toString());
	}

	@Test
	public void testedBySingleTestSuite() {
		StringFilter testNames = JavaSrcModule.with().name("suited")
				.testJava("test").testedBy("com.example.TestSuite").end()
				.testClassNameDefinition();

		assertTrue(testNames.matches("com.example.TestSuite"));
		assertFalse(testNames.matches("com.example.SomethingElse"));
	}

	@Test
	public void testedByManyTestsWithCustomNamingConvention() {
		StringFilter filter = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				return candidate.contains("a");
			}
		};

		StringFilter testNames = JavaSrcModule.with().name("tested")
				.testJava("test").testedBy(filter).end()
				.testClassNameDefinition();

		assertTrue(testNames.matches("a"));
		assertFalse(testNames.matches("b"));
	}

	@Test
	public void defaultEncodingIsUtf() {
		assertEquals(StandardCharsets.UTF_8,
				JavaSrcModule.with().end().encoding());
	}

	@Test
	public void encodingIsUsedToCompileMainAndTestClasses() {
		Charset encoding = Charset.forName("ISO-8859-1");
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").encoding(encoding).end();

		JavaClasses main = (JavaClasses) mod.mainArtifact();
		assertSame(encoding, main.encoding());
		JavaClasses test = (JavaClasses) mod.testArtifact();
		assertSame(encoding, test.encoding());
	}

	@Test
	public void rawCompilerArgsAreUsedToCompileMainAndTestClasses() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test")
				.rawCompilerArgs("-bootclasspath", "/mock/jdk-1.7").end();

		JavaClasses main = (JavaClasses) mod.mainArtifact();
		assertEquals(
				"[-Xlint, -Xlint:-serial, --release, 11, -g, -bootclasspath, /mock/jdk-1.7]",
				main.javacOptions().toString());
		JavaClasses test = (JavaClasses) mod.testArtifact();
		assertEquals(
				"[-Xlint, -Xlint:-serial, --release, 11, -g, -bootclasspath, /mock/jdk-1.7]",
				test.javacOptions().toString());
	}

	@Test
	public void noCharacteristicsByDefault() {
		assertTrue(JavaSrcModule.with().name("mod").mainJava("src").end()
				.characteristics().isEmpty());
	}

	@Test
	public void characteristicsReturnsGivenStandardCharacteristics() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.has(ProductionConfiguration.class).has(ProductionCode.class)
				.end();

		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$ProductionCode,"
						+ " interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$ProductionConfiguration]",
				mod.characteristics().toString());
	}

	@Test
	public void doesHaveTellsIfModuleHasTheAskedCharacteristicOrItsSubType() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.has(ProductionCode.class).end();

		assertTrue(mod.doesHave(ProductionCode.class));
		assertTrue(mod.doesHave(ProductionRuntimeData.class));
		assertTrue(mod.doesHave(JavaModuleCharacteristic.class));

		assertFalse(mod.doesHave(ProductionConfiguration.class));
		assertFalse(mod.doesHave(TestUtility.class));
	}

	@Test
	public void havingCharacteristicDoesNotImplyHavingItsSubType() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.has(ProductionRuntimeData.class).end();

		assertFalse(mod.doesHave(ProductionCode.class));
	}

	private interface CustomCharacteristic extends TestRuntimeData {
		// just a marker
	}

	@Test
	public void customCharacteristic() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.has(CustomCharacteristic.class).has(BuildUtility.class).end();

		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.JavaSrcModuleTest$CustomCharacteristic,"
						+ " interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$BuildUtility]",
				mod.characteristics().toString());
		assertTrue(mod.doesHave(CustomCharacteristic.class));
		assertTrue(mod.doesHave(TestRuntimeData.class));
		assertTrue(mod.doesHave(BuildUtility.class));

		assertFalse(mod.doesHave(TestCode.class));
	}

	@Test
	public void javaSrcModuleLikeAnotherIsReallyLikeIt() {
		JavaSrcModule m1 = JavaSrcModule.with()
				.codeFormatter(new CodeFormatterPolicy())
				.codeStyle(CodeStylePolicy.defaultsExcept()
						.fail(CodeStyle.DEAD_CODE).end())
				.encoding(Charset.forName("ISO-8859-15")).has(TestCode.class)
				.locationUnderWsRoot("mods/mod")
				.mainDeps(JavaBinModule
						.providing(Source.underWsroot("bin.jar")).end())
				.mainJava("src").mainResources("res")
				.mainRuntimeDeps(JavaBinModule
						.providing(Source.underWsroot("runbin.jar")).end())
				.name("mod")
				.testDeps(JavaBinModule
						.providing(Source.underWsroot("testbin.jar")).end())
				.testedBy("com.example.TestSuite").testJava("test")
				.testResources("testres")
				.testRuntimeDeps(JavaBinModule
						.providing(Source.underWsroot("testrunbin.jar")).end())
				.end();

		JavaSrcModule m2 = JavaSrcModule.like(m1).end();

		assertSame(m1.codeFormatterPolicy(), m2.codeFormatterPolicy());
		assertSame(m1.codeStylePolicy(), m2.codeStylePolicy());
		assertSame(m1.encoding(), m2.encoding());
		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$TestCode]",
				m2.characteristics().toString());
		assertEquals(m1.locationUnderWsRoot(), m2.locationUnderWsRoot());
		assertEquals("[bin.jar]", m2.mainDepsForCompilation().toString());
		assertEquals("[src]", m2.mainJavas().toString());
		assertEquals("[res]", m2.mainResources().toString());
		assertEquals("[runbin.jar]", m2.mainDepsForRunOnly().toString());
		assertEquals("mod", m2.name());
		assertEquals("[testbin.jar]",
				m2.testDepsForCompilationExcludingMainDeps().toString());
		assertEquals("StringFilterByEquality:com.example.TestSuite",
				m2.testClassNameDefinition().toString());
		assertEquals("[test]", m2.testJavas().toString());
		assertEquals("[testres]", m2.testResources().toString());
		assertEquals("[testrunbin.jar]",
				m2.testDepsForRunOnlyExcludingMainDeps().toString());
	}

	// -------------------------------------------
	// dependencies
	// -------------------------------------------

	// direct getters

	@Test
	public void directDepGettersWhenModuleHasNoDeps() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").end();

		assertEquals("[]", mod.mainDepsForCompilation().toString());
		assertEquals("[]", mod.mainDepsForRunOnly().toString());

		assertEquals("[]",
				mod.testDepsForCompilationExcludingMainDeps().toString());
		assertEquals("[]",
				mod.testDepsForRunOnlyExcludingMainDeps().toString());
	}

	@Test
	public void directDepGettersWhenModuleHasAllKindsOfDeps() {
		JavaBinModule mainCompDep = JavaBinModule
				.providing(Source.underWsroot("mainCompDep.jar")).end();
		JavaBinModule mainRuntimeDep = JavaBinModule
				.providing(Source.underWsroot("mainRuntimeDep.jar")).end();
		JavaBinModule testCompDep = JavaBinModule
				.providing(Source.underWsroot("testCompDep.jar")).end();
		JavaBinModule testtimeDep = JavaBinModule
				.providing(Source.underWsroot("testtimeDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(mainCompDep)
				.mainRuntimeDeps(mainRuntimeDep).testDeps(testCompDep)
				.testRuntimeDeps(testtimeDep).end();

		assertEquals("[mainCompDep.jar]",
				mod.mainDepsForCompilation().toString());
		assertEquals("[mainRuntimeDep.jar]",
				mod.mainDepsForRunOnly().toString());

		assertEquals("[testCompDep.jar]",
				mod.testDepsForCompilationExcludingMainDeps().toString());
		assertEquals("[testtimeDep.jar]",
				mod.testDepsForRunOnlyExcludingMainDeps().toString());
	}

	// non-cumulative cases

	@Test
	public void effectiveDepsWhenModuleHasOnlyCompileTimeDeps() {
		JavaBinModule compDep = JavaBinModule
				.providing(Source.underWsroot("compDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.mainDeps(compDep).testJava("test").end();

		assertEquals("[compDep.jar]",
				mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod, compDep.jar]",
				mod.effectivePathForMainRuntime().toString());
		assertEquals("[mod, compDep.jar]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[mod, compDep.jar]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveDepsWhenModuleHasCompileAndRuntimeDeps() {
		JavaBinModule compDep = JavaBinModule
				.providing(Source.underWsroot("compDep.jar")).end();
		JavaBinModule runDep = JavaBinModule
				.providing(Source.underWsroot("runDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(compDep).mainRuntimeDeps(runDep)
				.end();

		assertEquals("[compDep.jar]",
				mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod, compDep.jar, runDep.jar]",
				mod.effectivePathForMainRuntime().toString());
		assertEquals("[mod, compDep.jar]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[mod, compDep.jar, runDep.jar]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveDepsWhenModuleHasOnlyTestCompileDeps() {
		JavaBinModule testCompDep = JavaBinModule
				.providing(Source.underWsroot("testCompDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testDeps(testCompDep).end();

		assertEquals("[]", mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod]", mod.effectivePathForMainRuntime().toString());
		assertEquals("[testCompDep.jar, mod]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[testCompDep.jar, mod]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveDepsWhenModuleHasOnlyTestCompileAndTestRuntimeDeps() {
		JavaBinModule testCompDep = JavaBinModule
				.providing(Source.underWsroot("testCompDep.jar")).end();
		JavaBinModule testtimeDep = JavaBinModule
				.providing(Source.underWsroot("testtimeDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testDeps(testCompDep)
				.testRuntimeDeps(testtimeDep).end();

		assertEquals("[]", mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod]", mod.effectivePathForMainRuntime().toString());
		assertEquals("[testCompDep.jar, mod]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[testCompDep.jar, testtimeDep.jar, mod]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveDepsWhenModuleHasAllKindsOfDeps() {
		JavaBinModule mainCompDep = JavaBinModule
				.providing(Source.underWsroot("mainCompDep.jar")).end();
		JavaBinModule mainRuntimeDep = JavaBinModule
				.providing(Source.underWsroot("mainRuntimeDep.jar")).end();
		JavaBinModule testCompDep = JavaBinModule
				.providing(Source.underWsroot("testCompDep.jar")).end();
		JavaBinModule testtimeDep = JavaBinModule
				.providing(Source.underWsroot("testtimeDep.jar")).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(mainCompDep)
				.mainRuntimeDeps(mainRuntimeDep).testDeps(testCompDep)
				.testRuntimeDeps(testtimeDep).end();

		assertEquals("[mainCompDep.jar]",
				mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod, mainCompDep.jar, mainRuntimeDep.jar]",
				mod.effectivePathForMainRuntime().toString());
		assertEquals("[testCompDep.jar, mod, mainCompDep.jar]",
				mod.effectivePathForTestCompile().toString());
		assertEquals(
				"[testCompDep.jar, testtimeDep.jar, mod, mainCompDep.jar, mainRuntimeDep.jar]",
				mod.effectivePathForTestRuntime().toString());
	}

	// cumulative deps

	// main dep of main dep

	@Test
	public void effectiveDepsWithCompileDepOfCompileDep() {
		JavaBinModule utilOfProdUtilJar = JavaBinModule
				.providing(Source.underWsroot("utilOfProdUtil.jar")).end();
		JavaSrcModule prodUtil = JavaSrcModule.with().name("prodUtil")
				.mainJava("src").mainDeps(utilOfProdUtilJar).testJava("test")
				.end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(prodUtil).end();

		assertEquals("[prodUtil]",
				mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod, prodUtil, utilOfProdUtil.jar]",
				mod.effectivePathForMainRuntime().toString());
		assertEquals("[mod, prodUtil]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[mod, prodUtil, utilOfProdUtil.jar]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveRuntimeDepsWithRuntimeDepOfCompileDep() {
		JavaBinModule utilOfProdUtilJar = JavaBinModule
				.providing(Source.underWsroot("utilOfProdUtil.jar")).end();
		JavaBinModule prodUtilJar = JavaBinModule
				.providing(Source.underWsroot("prodUtil.jar"))
				.runtimeDeps(utilOfProdUtilJar).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").mainDeps(prodUtilJar).testJava("test").end();

		assertEquals("[prodUtil.jar]",
				mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod, prodUtil.jar, utilOfProdUtil.jar]",
				mod.effectivePathForMainRuntime().toString());
		assertEquals("[mod, prodUtil.jar]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[mod, prodUtil.jar, utilOfProdUtil.jar]",
				mod.effectivePathForTestRuntime().toString());
	}

	// main dep of test dep

	@Test
	public void effectiveRuntimeDepsWithCompileDepOfTestCompileDep() {
		JavaBinModule utilOfTestUtilJar = JavaBinModule
				.providing(Source.underWsroot("utilOfTestUtil.jar")).end();
		JavaSrcModule testUtil = JavaSrcModule.with().name("testUtil")
				.mainJava("src").testJava("test").mainDeps(utilOfTestUtilJar)
				.end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testDeps(testUtil).end();

		assertEquals("[]", mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod]", mod.effectivePathForMainRuntime().toString());
		assertEquals("[testUtil, mod]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[testUtil, utilOfTestUtil.jar, mod]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void effectiveRuntimeDepsWithRuntimeDepOfTestCompileDep() {
		JavaBinModule utilOfTestUtilJar = JavaBinModule
				.providing(Source.underWsroot("utilOfTestUtil.jar")).end();
		JavaBinModule testUtilJar = JavaBinModule
				.providing(Source.underWsroot("testUtil.jar"))
				.runtimeDeps(utilOfTestUtilJar).end();

		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testJava("test").testDeps(testUtilJar).end();

		assertEquals("[]", mod.effectivePathForMainForCompile().toString());
		assertEquals("[mod]", mod.effectivePathForMainRuntime().toString());
		assertEquals("[testUtil.jar, mod]",
				mod.effectivePathForTestCompile().toString());
		assertEquals("[testUtil.jar, utilOfTestUtil.jar, mod]",
				mod.effectivePathForTestRuntime().toString());
	}

	@Test
	public void mainClassesHaveDebugEnabledByDefault() {
		JavaSrcModule mod = JavaSrcModule.with().name("simple").mainJava("src")
				.end();
		JavaClasses classes = (JavaClasses) mod.mainArtifact();

		assertTrue(classes.debug());
	}

	@Test
	public void testClassesHaveDebugEnabledByDefault() {
		JavaSrcModule mod = JavaSrcModule.with().name("simple").mainJava("src")
				.testJava("test").end();
		JavaClasses classes = (JavaClasses) mod.testArtifact();

		assertTrue(classes.debug());
	}

	@Test
	public void moduleWithTestsHasDefaultTestClassNameFilter() {
		JavaSrcModule mod = JavaSrcModule.with().name("tested").mainJava("src")
				.testJava("test").end();

		assertTrue(mod
				.testClassNameDefinition() instanceof DefaultTestClassNameFilter);
	}

	@Test
	public void alsoTestlessModuleHasDefaultTestClassNameFilter() {
		JavaSrcModule mod = JavaSrcModule.with().name("testless")
				.mainJava("src").end();

		assertTrue(mod
				.testClassNameDefinition() instanceof DefaultTestClassNameFilter);
	}

	@Test
	public void testlessModuleTestClassNameFilterCanBeExplicitlySetEvenIfItHasNoUse() {
		StringFilter filter = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				throw new UnsupportedOperationException("not to be called");
			}
		};
		JavaSrcModule mod = JavaSrcModule.with().name("testless")
				.testedBy(filter).mainJava("src").end();

		assertSame(filter, mod.testClassNameDefinition());
	}

	@Test
	public void testClassNameDefinitionReturnsGivenFilter() {
		StringFilter filter = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				throw new UnsupportedOperationException("not to be called");
			}
		};
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.testJava("test").testedBy(filter).end();

		assertSame(filter, mod.testClassNameDefinition());
	}

	@Test
	public void sourceComplianceOfMainAndTestClassesIsTheJavaComplianceOfTheModule() {
		JavaCompliance nonDefaultCompliance = JavaCompliance.JAVA_1_6;
		// inner test:
		assertFalse(nonDefaultCompliance.equals(
				JavaSrcModule.with().name("defaults").end().javaCompliance()));

		JavaSrcModule mod = JavaSrcModule.with().name("mod")
				.javaCompliance(nonDefaultCompliance).mainJava("src")
				.testJava("test").end();

		JavaClasses mainClasses = (JavaClasses) mod.mainArtifact();
		JavaClasses testClasses = (JavaClasses) mod.testArtifact();

		assertEquals(nonDefaultCompliance, mainClasses.sourceCompliance());
		assertEquals(nonDefaultCompliance, testClasses.sourceCompliance());
	}

	@Test
	public void mainClassesHasCorrectKindOfClassesWhenScalaInUse() {
		ScalaVersion scala = ScalaVersion._2_11_7();
		JavaModule dep = JavaBinModule.providing(Source.underWsroot("dep"))
				.end();
		JavaSrcModule mod = JavaSrcModule.with().name("mod").scalaVersion(scala)
				.mainJava("src/main/java").mainScala("src/main/scala")
				.mainDeps(dep).end();
		JavaClasses mainClasses = (JavaClasses) mod.mainArtifact();

		assertEquals("[mod-main-classes-from-scala, dep]",
				mainClasses.classLocations().toString());
		assertEquals("[mod-main-classes-from-scala]",
				mainClasses.resourceDirs().toString());

		ScalaClasses scalaClasses = (ScalaClasses) mainClasses.classLocations()
				.iterator().next();
		assertEquals("[mod/src/main/java, mod/src/main/scala]",
				scalaClasses.srcDirs().toString());
		assertEquals("[dep]", scalaClasses.classLocations().toString());
		assertSame(scala, scalaClasses.scala());
	}

	@Test
	public void testClassesHasCorrectKindOfClassesWhenScalaInUse() {
		ScalaVersion scala = ScalaVersion._2_11_7();
		JavaModule dep = JavaBinModule.providing(Source.underWsroot("dep"))
				.end();
		JavaSrcModule mod = JavaSrcModule.with().name("mod").scalaVersion(scala)
				.mainJava("src/main/java").mainScala("src/main/scala")
				.testJava("src/test/java").testScala("src/test/scala")
				.mainDeps(dep).end();
		JavaClasses testClasses = (JavaClasses) mod.testArtifact();

		assertEquals("[mod-test-classes-from-scala, mod-main-classes, dep]",
				testClasses.classLocations().toString());
		assertEquals("[mod-test-classes-from-scala]",
				testClasses.resourceDirs().toString());

		ScalaClasses scalaClasses = (ScalaClasses) testClasses.classLocations()
				.iterator().next();
		assertEquals("[mod/src/test/java, mod/src/test/scala]",
				scalaClasses.srcDirs().toString());
		assertEquals("[mod-main-classes, dep]",
				scalaClasses.classLocations().toString());
		assertSame(scala, scalaClasses.scala());
	}

	@Test
	public void mainClassesHasCorrectKindOfClassesWhenKotlinInUse() {
		KotlinVersion kotlin = KotlinVersion._1_3_60();

		JavaModule dep = JavaBinModule.providing(Source.underWsroot("dep"))
				.end();
		JavaSrcModule mod = JavaSrcModule.with().name("mod")
				.kotlinVersion(kotlin).mainJava("src/main/java")
				.mainResources("src/main/resources").mainDeps(dep).end();

		KotlinAndJavaClasses mainClasses = (KotlinAndJavaClasses) mod
				.mainArtifact();

		assertEquals("[mod/src/main/java]", mainClasses.srcDirs().toString());
		assertEquals("[mod/src/main/resources]",
				mainClasses.resourceDirs().toString());
		assertEquals("[dep]", mainClasses.classLocations().toString());
	}

	@Test
	public void testClassesHasCorrectKindOfClassesWhenKotlinInUse() {
		KotlinVersion kotlin = KotlinVersion._1_3_60();

		JavaModule dep = JavaBinModule.providing(Source.underWsroot("dep"))
				.end();
		JavaSrcModule mod = JavaSrcModule.with().name("mod")
				.kotlinVersion(kotlin).mainJava("src/main/java")
				.testJava("src/test/java").testResources("src/test/resources")
				.mainDeps(dep).end();

		KotlinAndJavaClasses testClasses = (KotlinAndJavaClasses) mod
				.testArtifact();

		assertEquals("[mod/src/test/java]", testClasses.srcDirs().toString());
		assertEquals("[mod/src/test/resources]",
				testClasses.resourceDirs().toString());
		assertEquals("[mod-main-classes, dep]",
				testClasses.classLocations().toString());
	}

}