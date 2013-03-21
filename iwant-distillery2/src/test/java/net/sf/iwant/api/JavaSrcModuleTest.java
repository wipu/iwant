package net.sf.iwant.api;

import junit.framework.TestCase;
import net.sf.iwant.api.JavaSrcModule.IwantSrcModuleSpex;

public class JavaSrcModuleTest extends TestCase {

	public void testToStringIsTheName() {
		assertEquals("m1", JavaSrcModule.with().name("m1").end().toString());
		assertEquals("m2", JavaSrcModule.with().name("m2").end().toString());
	}

	public void testParentDirectoryNormalization() {
		assertEquals("a", JavaSrcModule.with().name("a").end()
				.locationUnderWsRoot());
		assertEquals("a", JavaSrcModule.with().name("a").relativeParentDir("")
				.end().locationUnderWsRoot());
		assertEquals("dir/a",
				JavaSrcModule.with().name("a").relativeParentDir("dir").end()
						.locationUnderWsRoot());
		assertEquals("dir/a",
				JavaSrcModule.with().name("a").relativeParentDir("dir/").end()
						.locationUnderWsRoot());
	}

	public void testRelativeWsRoot() {
		assertEquals("..", JavaSrcModule.with().name("a").end()
				.relativeWsRoot());
		assertEquals("../..",
				JavaSrcModule.with().name("a").relativeParentDir("one").end()
						.relativeWsRoot());
		assertEquals("../../..", JavaSrcModule.with().name("a")
				.relativeParentDir("one/two").end().relativeWsRoot());
	}

	public void testLocationUnderWsRootFromParentAndName() {
		assertEquals("simple", JavaSrcModule.with().name("simple").end()
				.locationUnderWsRoot());
		assertEquals("one/nested", JavaSrcModule.with().name("nested")
				.relativeParentDir("one").end().locationUnderWsRoot());
		assertEquals("one/two/nested", JavaSrcModule.with().name("nested")
				.relativeParentDir("one/two").end().locationUnderWsRoot());
	}

	public void testLocationThatDiffersFromName() {
		JavaSrcModule module = JavaSrcModule.with().name("module-name")
				.locationUnderWsRoot("parent/dir-name").end();

		assertEquals("../..", module.relativeWsRoot());
		assertEquals("parent/dir-name", module.locationUnderWsRoot());
	}

	public void testSettingBothParentAndLocationIsAnError() {
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

	public void testMainJavaAsPathFromSourcelessSrcModule() {
		assertNull(JavaSrcModule.with().name("srcless").end().mainJavaAsPath());
	}

	public void testMainJavaAsPathFromNormalSrcModule() {
		assertEquals("simple/src", JavaSrcModule.with().name("simple")
				.mainJava("src").end().mainJavaAsPath().name());
		assertEquals(
				"parent/more-nesting/src/main/java",
				JavaSrcModule.with().name("more-nesting")
						.relativeParentDir("parent").mainJava("src/main/java")
						.end().mainJavaAsPath().name());
	}

	public void testMainJavaAsPathFromCodeGenerationModule() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();

		Target mainJava = (Target) module.mainJavaAsPath();
		assertSame(generatedSrc, mainJava);
	}

	public void testTestJavaAsPathWithNullAndNonNull() {
		assertNull(JavaSrcModule.with().name("testless").end().testJavaAsPath());
		assertEquals("tested/test", JavaSrcModule.with().name("tested")
				.testJava("test").end().testJavaAsPath().toString());
	}

	public void testSourcelessSrcModuleHasNoMainArtifact() {
		JavaSrcModule module = JavaSrcModule.with().name("srcless").end();

		assertNull(module.mainArtifact());
	}

	public void testMainArtifactOfCodeGenerationModule() {
		Target generatedSrc = new HelloTarget("generated-src",
				"in reality this would be a src directory generated from src-for-generator");
		Target generatedClasses = Concatenated.named("generated-classes")
				.string("in reality this would be compiled from: ")
				.pathTo(generatedSrc).end();
		JavaSrcModule module = JavaSrcModule.with()
				.name("code-generating-module")
				.relativeParentDir("subdir1/subdir2")
				.exportsClasses(generatedClasses, generatedSrc).end();

		Target mainArtifact = (Target) module.mainArtifact();
		assertSame(generatedClasses, mainArtifact);
	}

	public void testMainArtifactOfMinimalSrcModuleIsJavaClassesCompiledFromSource() {
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src").end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("simple/src", mainArtifact.srcDir().name());
		assertEquals("simple/src", mainArtifact.srcDir().toString());
		assertEquals("[]", mainArtifact.classLocations().toString());
	}

	public void testMainArtifactOfOfSrcModuleThatHasDependencies() {
		JavaSrcModule util1 = JavaSrcModule.with().name("util1")
				.mainJava("src/main/java").end();
		JavaSrcModule util2 = JavaSrcModule.with().name("util2")
				.mainJava("src/main/java").end();
		JavaSrcModule module = JavaSrcModule.with().name("simple")
				.mainJava("src/main/java").mainDeps(util1, util2).end();

		JavaClasses mainArtifact = (JavaClasses) module.mainArtifact();

		assertEquals("simple/src/main/java", mainArtifact.srcDir().name());
		assertEquals("[util1-main-classes, util2-main-classes]", mainArtifact
				.classLocations().toString());
	}

	public void testModulesAreComparedByName() {
		JavaModule srcA = JavaSrcModule.with().name("a").end();
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaModule binA = JavaBinModule.named("a").inside(libs);

		assertTrue(srcA.compareTo(binA) == 0);
		assertTrue(binA.compareTo(srcA) == 0);

		assertTrue(srcA.compareTo(libs) < 0);
		assertTrue(binA.compareTo(libs) < 0);

		assertTrue(libs.compareTo(srcA) > 0);
		assertTrue(libs.compareTo(binA) > 0);
	}

	public void testTestArtifactIsNullWhenNoTests() {
		assertNull(JavaSrcModule.with().name("testless").mainJava("src").end()
				.testArtifact());
	}

	public void testTestArtifactIsWhenThereAreTests() {
		JavaClasses tests = (JavaClasses) JavaSrcModule.with().name("tested")
				.mainJava("src").testJava("test").end().testArtifact();

		assertEquals("tested-test-classes", tests.name());
		assertEquals("tested/test", tests.srcDir().toString());
		assertEquals("[tested-main-classes]", tests.classLocations().toString());
	}

	public void testTestArtifactWithMainAndTestDeps() {
		JavaClasses tests = (JavaClasses) JavaSrcModule
				.with()
				.name("tested2")
				.mainJava("src")
				.mainDeps(
						JavaBinModule.providing(Source
								.underWsroot("main-lib.jar")))
				.testJava("test")
				.testDeps(
						JavaBinModule.providing(Source
								.underWsroot("test-lib.jar"))).end()
				.testArtifact();

		assertEquals("tested2-test-classes", tests.name());
		assertEquals("tested2/test", tests.srcDir().toString());
		assertEquals("[tested2-main-classes, main-lib.jar, test-lib.jar]",
				tests.classLocations().toString());
	}

	public void testTestArtifactWhenNoMainJava() {
		JavaClasses tests = (JavaClasses) JavaSrcModule.with()
				.name("only-tests").testJava("test").end().testArtifact();

		assertEquals("only-tests-test-classes", tests.name());
		assertEquals("only-tests/test", tests.srcDir().toString());
		assertEquals("[]", tests.classLocations().toString());
	}

}
