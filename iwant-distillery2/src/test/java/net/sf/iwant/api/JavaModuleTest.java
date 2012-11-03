package net.sf.iwant.api;

import junit.framework.TestCase;
import net.sf.iwant.entry3.TargetMock;

public class JavaModuleTest extends TestCase {

	public void testImplicitLibraryModule() {
		Path jar = new TargetMock("lib.jar");
		JavaModule lib = JavaModule.implicitLibrary(jar);

		assertFalse(lib.isExplicit());

		assertEquals("lib.jar", lib.name());
		assertNull(lib.locationUnderWsRoot());
		assertNull(lib.mainJava());
		assertNull(lib.mainDeps());
		assertSame(jar, lib.mainClasses());
	}

	public void testNormalModule() {
		Path jar = new TargetMock("lib.jar");
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
	}

	public void testComparationIsDelegatedToMainClassesName() {
		JavaModule a1 = JavaModule.implicitLibrary(new TargetMock("a1"));
		JavaModule a2 = JavaModule.implicitLibrary(new TargetMock("a2"));
		JavaModule b = JavaModule.with().name("b").locationUnderWsRoot("d/a")
				.mainJava("src").mainDeps(a1).end();

		JavaModule bClone = JavaModule.implicitLibrary(new TargetMock("b"));

		assertTrue(a1.compareTo(b) < 0);
		assertTrue(b.compareTo(a1) > 0);

		assertTrue(a1.compareTo(a2) < 0);

		assertTrue(b.compareTo(bClone) == 0);
	}

}
