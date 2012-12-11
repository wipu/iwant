package net.sf.iwant.api;

import junit.framework.TestCase;
import net.sf.iwant.entry3.TargetMock;

public class JavaModuleTest extends TestCase {

	public void testImplicitLibraryModule() {
		Path jar = TargetMock.ingredientless("lib.jar");
		JavaModule lib = JavaModule.implicitLibrary(jar);

		assertFalse(lib.isExplicit());

		assertEquals("lib.jar", lib.name());
		assertNull(lib.locationUnderWsRoot());
		assertNull(lib.mainJava());
		assertTrue(lib.mainDeps().isEmpty());
		assertSame(jar, lib.mainClasses());
		assertNull(lib.testJava());
		assertTrue(lib.testDeps().isEmpty());
		assertNull(lib.testClasses());
	}

	public void testNormalModuleWithMainJavaOnly() {
		Path jar = TargetMock.ingredientless("lib.jar");
		JavaModule jarLib = JavaModule.implicitLibrary(jar);

		JavaModule a = JavaModule.with().name("a").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(jarLib).end();

		assertTrue(a.isExplicit());

		assertEquals("a", a.name());
		assertEquals("d/a", a.locationUnderWsRoot());
		assertEquals("src", a.mainJava());
		assertTrue(a.mainDeps().contains(jarLib));

		JavaClasses aMainClasses = (JavaClasses) a.mainClasses();
		assertEquals("a-main-classes", aMainClasses.name());
		assertEquals("d/a/src", aMainClasses.srcDir().toString());
		assertTrue(aMainClasses.classLocations().contains(jar));

		assertNull(a.testClasses());
	}

	public void testBasicsOfTestClassesOfModule() {
		Path jar = TargetMock.ingredientless("lib.jar");
		JavaModule jarLib = JavaModule.implicitLibrary(jar);
		Path testUtilJar = TargetMock.ingredientless("testlib.jar");
		JavaModule testUtilJarLib = JavaModule.implicitLibrary(testUtilJar);

		JavaModule a = JavaModule.with().name("a").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(jarLib).testJava("test")
				.testDeps(testUtilJarLib).end();

		JavaClasses aTestClasses = (JavaClasses) a.testClasses();
		assertEquals("a-test-classes", aTestClasses.name());
		assertEquals("d/a/test", aTestClasses.srcDir().toString());
	}

	public void testTestClassesHasBothTestAndMainDepsAsDeps() {
		Path jar = TargetMock.ingredientless("lib.jar");
		JavaModule jarLib = JavaModule.implicitLibrary(jar);
		Path testUtilJar = TargetMock.ingredientless("testlib.jar");
		JavaModule testUtilJarLib = JavaModule.implicitLibrary(testUtilJar);

		JavaModule a = JavaModule.with().name("a").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(jarLib).testJava("test")
				.testDeps(testUtilJarLib).end();

		JavaClasses aTestClasses = (JavaClasses) a.testClasses();
		assertEquals("a-test-classes", aTestClasses.name());
		assertEquals("d/a/test", aTestClasses.srcDir().toString());

		// explicit test dep:
		assertTrue(aTestClasses.classLocations().contains(testUtilJar));
		// main dep is an implicit test dep.
		assertTrue(aTestClasses.classLocations().contains(jar));
	}

	public void testNoDuplicatesEvenIfSameDepDeclaredAsMainAndTestDep() {
		Path jar = TargetMock.ingredientless("lib.jar");
		JavaModule jarLib = JavaModule.implicitLibrary(jar);
		Path testUtilJar = TargetMock.ingredientless("testlib.jar");
		JavaModule testUtilJarLib = JavaModule.implicitLibrary(testUtilJar);

		JavaModule a = JavaModule.with().name("a").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(jarLib).testJava("test")
				.testDeps(jarLib, testUtilJarLib).end();

		JavaClasses aTestClasses = (JavaClasses) a.testClasses();
		assertEquals("a-test-classes", aTestClasses.name());
		assertEquals("d/a/test", aTestClasses.srcDir().toString());

		// jar only once:
		assertEquals("[lib.jar, testlib.jar]", aTestClasses.classLocations()
				.toString());
	}

	public void testComparationIsDelegatedToMainClassesName() {
		JavaModule a1 = JavaModule.implicitLibrary(TargetMock
				.ingredientless("a1"));
		JavaModule a2 = JavaModule.implicitLibrary(TargetMock
				.ingredientless("a2"));
		JavaModule b = JavaModule.with().name("b").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(a1).end();

		JavaModule bClone = JavaModule.implicitLibrary(TargetMock
				.ingredientless("b"));

		assertTrue(a1.compareTo(b) < 0);
		assertTrue(b.compareTo(a1) > 0);

		assertTrue(a1.compareTo(a2) < 0);

		assertTrue(b.compareTo(bClone) == 0);
	}

	public void testImplicitLibraryModuleDependsOnPathDependencyAsAnotherImplicitLibrary() {
		Path lib1 = TargetMock.ingredientless("lib1");
		TargetMock lib2 = new TargetMock("lib2");
		lib2.hasIngredients(lib1);
		JavaModule lib2Module = JavaModule.implicitLibrary(lib2);

		assertFalse(lib2Module.isExplicit());

		assertEquals("lib2", lib2Module.name());
		assertEquals(1, lib2Module.mainDeps().size());

		JavaModule lib1Module = lib2Module.mainDeps().get(0);
		assertEquals("lib1", lib1Module.name());
		assertSame(lib1, lib1Module.mainClasses());
	}

	public void testNoMainJavaMeansNullJavaClasses() {
		JavaModule srcless = JavaModule.with().name("srcless").end();
		assertNull(srcless.mainClasses());
	}

	public void testClasslessDependencyIsNotSeenInClasspathOfMainClasses() {
		JavaModule srcless = JavaModule.with().name("srcless").end();
		JavaModule user = JavaModule.with().name("depending").mainJava("src")
				.mainDeps(srcless).end();
		JavaClasses userClasses = (JavaClasses) user.mainClasses();
		assertTrue(userClasses.classLocations().isEmpty());
	}

}
