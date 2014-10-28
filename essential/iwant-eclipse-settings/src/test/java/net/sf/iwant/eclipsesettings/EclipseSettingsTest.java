package net.sf.iwant.eclipsesettings;

import java.io.File;
import java.util.Arrays;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.apimocks.TargetMock;

public class EclipseSettingsTest extends IwantTestCase {

	@Override
	protected void moreSetUp() {
		File wsdefdef = wsRootHasDirectory("as-someone/i-have/wsdefdef");
		wsRootHasDirectory("as-someone/i-have/wsdef");
		seCtx.wsInfo().hasWsdefdefModule(wsdefdef);
		seCtx.wsInfo().hasWsName(getClass().getSimpleName());
		seCtx.wsInfo().hasRelativeAsSomeone("as-someone");
	}

	private void assertDotClasspathContains(String project, String fragment) {
		assertFileContains(project + "/.classpath", fragment);
	}

	private void assertDotProjectContains(String project, String fragment) {
		assertFileContains(project + "/.project", fragment);
	}

	private void assertFileContains(String filename, String fragment) {
		String fullActual = contentOfFileUnderWsRoot(filename);
		if (!fullActual.contains(fragment)) {
			assertEquals(fragment, fullActual);
		}
		if (fullActual.indexOf(fragment) != fullActual.lastIndexOf(fragment)) {
			assertEquals("File contains fragment more than once:\n" + fragment,
					fullActual);
		}
	}

	public void testModulesCanBeAddedInManyPartsBothAsVarargsAndAsCollections() {
		JavaSrcModule m1 = JavaSrcModule.with().name("m1").end();
		JavaSrcModule m2 = JavaSrcModule.with().name("m2").end();
		JavaSrcModule m3 = JavaSrcModule.with().name("m3").end();
		JavaSrcModule m4 = JavaSrcModule.with().name("m4").end();
		JavaSrcModule m5 = JavaSrcModule.with().name("m5").end();
		EclipseSettings es = EclipseSettings.with().modules(m1, m2)
				.modules(Arrays.asList(m3, m4)).modules(m5).end();

		assertEquals("[m1, m2, m3, m4, m5]", es.modules().toString());
	}

	public void testModulesAreCollectedToSortedSetSoMultipleAddsAndWrongOrderDontAffectIt() {
		JavaSrcModule m1 = JavaSrcModule.with().name("m1").end();
		JavaSrcModule m2 = JavaSrcModule.with().name("m2").end();
		JavaSrcModule m3 = JavaSrcModule.with().name("m3").end();
		JavaSrcModule m4 = JavaSrcModule.with().name("m4").end();
		JavaSrcModule m5 = JavaSrcModule.with().name("m5").end();
		EclipseSettings es = EclipseSettings.with().modules(m5, m4)
				.modules(Arrays.asList(m3)).modules(m1, m2, m3, m4, m5).end();

		assertEquals("[m1, m2, m3, m4, m5]", es.modules().toString());
	}

	public void testMutationUsingWsdefdefAndWsdefAndAnotherModuleUsedByWsdef() {
		JavaModule iwantClasses = JavaBinModule.providing(
				TargetMock.ingredientless("iwant-classes"),
				TargetMock.ingredientless("combined-iwant-sources")).end();
		JavaModule wsdefdef = JavaSrcModule.with().name("test-wsdefdef")
				.locationUnderWsRoot("as-someone/i-have/wsdefdef")
				.mainJava("src/main/java").mainDeps(iwantClasses).end();
		wsRootHasDirectory("utils/wsdef-tools");
		JavaModule wsdefTools = JavaSrcModule.with().name("test-wsdef-tools")
				.locationUnderWsRoot("utils/wsdef-tools").mainJava("src").end();
		JavaModule wsdef = JavaSrcModule.with().name("test-wsdef")
				.locationUnderWsRoot("as-someone/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantClasses, wsdefTools)
				.end();
		EclipseSettings es = EclipseSettings.with()
				.modules(wsdefdef, wsdefTools, wsdef).name("es").end();

		es.mutate(seCtx);

		assertDotProjectContains("as-someone/i-have/wsdefdef",
				"<name>test-wsdefdef</name>");
		assertDotProjectContains("as-someone/i-have/wsdef",
				"<name>test-wsdef</name>");
		assertDotProjectContains("utils/wsdef-tools",
				"<name>test-wsdef-tools</name>");

		assertDotClasspathContains("as-someone/i-have/wsdefdef",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdefdef",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/iwant-classes\" sourcepath=\"" + cacheDir
						+ "/combined-iwant-sources\"/>");

		assertDotClasspathContains("utils/wsdef-tools",
				"<classpathentry kind=\"src\" path=\"src\"/>");

		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/iwant-classes\" sourcepath=\"" + cacheDir
						+ "/combined-iwant-sources\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry combineaccessrules=\"false\""
						+ " kind=\"src\" path=\"/test-wsdef-tools\"/>");
	}

	public void testNoSourcesMeansNoSources() {
		JavaModule srcless = JavaSrcModule.with().name("any")
				.locationUnderWsRoot("any").end();
		wsRootHasDirectory("any");

		EclipseSettings es = EclipseSettings.with().modules(srcless).name("es")
				.end();

		es.mutate(seCtx);

		assertFalse(contentOfFileUnderWsRoot("any/.classpath").contains(
				"<classpathentry kind=\"src\" "));
	}

	public void testTestJavaAndTestDepsAffectDotClasspath() {
		JavaModule testTools1 = JavaBinModule.providing(
				TargetMock.ingredientless("test-tools-1"),
				TargetMock.ingredientless("test-tools-1-src")).end();
		JavaModule testTools2 = JavaBinModule.providing(
				TargetMock.ingredientless("test-tools-2-srcless"), null).end();

		JavaModule mod1 = JavaSrcModule.with().name("mod1")
				.locationUnderWsRoot("mod1").mainJava("src").testJava("tests1")
				.testDeps(testTools1).end();
		JavaModule mod2 = JavaSrcModule.with().name("mod2")
				.locationUnderWsRoot("mod2").mainJava("src2")
				.testJava("tests2").testDeps(testTools2).end();
		wsRootHasDirectory("mod1");
		wsRootHasDirectory("mod2");

		EclipseSettings es = EclipseSettings.with().modules(mod1, mod2)
				.name("es").end();

		es.mutate(seCtx);

		assertDotClasspathContains("mod1",
				"<classpathentry kind=\"src\" path=\"tests1\"/>");
		assertDotClasspathContains("mod2",
				"<classpathentry kind=\"src\" path=\"tests2\"/>");

		assertDotClasspathContains("mod1",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/test-tools-1\" sourcepath=\"" + cacheDir
						+ "/test-tools-1-src\"/>");
		assertDotClasspathContains("mod2",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/test-tools-2-srcless\"/>");
	}

	public void testSetOfAllMainAndTestDependencyBinaryClassesAndSourcesGetsRefreshedSoEclipseWontComplainAboutMissingRefs() {
		// 2 modules depend on this, but it's mentioned only once in refs:
		JavaModule binWithoutSources = JavaBinModule.providing(
				TargetMock.ingredientless("binWithoutSources")).end();
		// sources of this shall also be refreshed, they are dynamic:
		JavaModule binWithSources = JavaBinModule.providing(
				TargetMock.ingredientless("binWithSources"),
				TargetMock.ingredientless("binWithSources-src")).end();
		// source dep shall not be refreshed, eclipse can do that by itself:
		JavaSrcModule srcDep = JavaSrcModule.with().name("srcDep")
				.locationUnderWsRoot("srcDep").mainJava("src")
				.mainDeps(binWithoutSources).end();
		// not only main, but test deps also:
		JavaModule testUtilWithSources = JavaBinModule.providing(
				TargetMock.ingredientless("testUtilWithSources"),
				TargetMock.ingredientless("testUtilWithSources-src")).end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").testJava("test")
				.mainDeps(binWithoutSources, binWithSources, srcDep)
				.testDeps(testUtilWithSources).end();

		EclipseSettings es = EclipseSettings.with().modules(mod, srcDep)
				.name("es").end();
		es.mutate(seCtx);

		assertEquals(1, seCtx.targetsWantedAsPath().size());
		assertEquals("Concatenated {\n"
				+ "path-of:testUtilWithSources\nstring:'\n'\n"
				+ "path-of:testUtilWithSources-src\nstring:'\n'\n"
				+ "path-of:binWithoutSources\nstring:'\n'\n"
				+ "path-of:binWithSources\nstring:'\n'\n"
				+ "path-of:binWithSources-src\nstring:'\n'\n" + "}\n", seCtx
				.targetsWantedAsPath().get(0).contentDescriptor());
	}

	public void testClasspathIsGeneratedButWithWarningIfRefreshOfDepsFails() {
		JavaModule util = JavaBinModule.providing(
				TargetMock.ingredientless("util"),
				TargetMock.ingredientless("util-src")).end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").mainDeps(util)
				.end();

		seCtx.shallFailIwantAsPathWith(new RuntimeException(
				"compilation of util classes failed"));
		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		assertEquals(1, seCtx.targetsWantedAsPath().size());
		assertEquals("Concatenated {\n" + "path-of:util\nstring:'\n'\n"
				+ "path-of:util-src\nstring:'\n'\n" + "}\n", seCtx
				.targetsWantedAsPath().get(0).contentDescriptor());

		assertDotClasspathContains("mod",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/util\" sourcepath=\"" + cacheDir + "/util-src\"/>");
		assertEquals(
				"WARNING: Refresh of eclipse settings references failed:\n"
						+ "java.lang.RuntimeException: compilation of util classes failed\n"
						+ "", seCtx.err().toString());
	}

	public void testNameOfBinDepRefreshTarget() {
		JavaModule util = JavaBinModule.providing(
				TargetMock.ingredientless("util"),
				TargetMock.ingredientless("util-src")).end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").mainDeps(util)
				.end();

		EclipseSettings es1 = EclipseSettings.with().modules(mod).name("es1")
				.end();
		es1.mutate(seCtx);
		EclipseSettings es2 = EclipseSettings.with().modules(mod).name("es2")
				.end();
		es2.mutate(seCtx);

		assertEquals("es1.bin-refs", seCtx.targetsWantedAsPath().get(0).name());
		assertEquals("es2.bin-refs", seCtx.targetsWantedAsPath().get(1).name());
	}

	public void testDefaultJavaComplianceIs16() {
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		String corePrefs = "mod/.settings/org.eclipse.jdt.core.prefs";
		assertFileContains(corePrefs, "targetPlatform=1.6");
		assertFileContains(corePrefs, "compiler.compliance=1.6");
		assertFileContains(corePrefs, "compiler.source=1.6");
	}

	public void testJavaComplianceCanBeDefinedAs17() {
		JavaModule mod = JavaSrcModule.with().name("mod")
				.javaCompliance(JavaCompliance.JAVA_1_7)
				.locationUnderWsRoot("mod").mainJava("src").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		String corePrefs = "mod/.settings/org.eclipse.jdt.core.prefs";
		assertFileContains(corePrefs, "targetPlatform=1.7");
		assertFileContains(corePrefs, "compiler.compliance=1.7");
		assertFileContains(corePrefs, "compiler.source=1.7");
	}

	public void testJavaAndResourceDirsAreCreated() {
		JavaModule modWithAll = JavaSrcModule.with().name("mod-with-all")
				.locationUnderWsRoot("mod-with-all").mavenLayout().end();
		JavaModule modWithNone = JavaSrcModule.with().name("mod-with-none")
				.locationUnderWsRoot("mod-with-all").end();
		JavaBinModule binToBeExcluded = JavaBinModule.providing(
				Source.underWsroot("bin")).end();

		EclipseSettings es = EclipseSettings.with()
				.modules(modWithAll, modWithNone, binToBeExcluded).name("es")
				.end();
		es.mutate(seCtx);

		assertFalse(new File(wsRoot, "mod-with-none/src/main/java").exists());
		assertFalse(new File(wsRoot, "mod-with-none/src/main/resources")
				.exists());
		assertFalse(new File(wsRoot, "mod-with-none/src/test/java").exists());
		assertFalse(new File(wsRoot, "mod-with-none/src/test/resources")
				.exists());

		assertTrue(new File(wsRoot, "mod-with-all/src/main/java").exists());
		assertTrue(new File(wsRoot, "mod-with-all/src/main/resources").exists());
		assertTrue(new File(wsRoot, "mod-with-all/src/test/java").exists());
		assertTrue(new File(wsRoot, "mod-with-all/src/test/resources").exists());
	}

	public void testTestRuntimeDepenencyGoesToDotClasspath() {
		JavaModule rtBinTool = JavaBinModule.providing(
				TargetMock.ingredientless("rtBinTool"),
				TargetMock.ingredientless("rtBinTool-src")).end();

		JavaModule rtSrcTool = JavaSrcModule.with().name("rtSrcTool")
				.locationUnderWsRoot("rtSrcTool").mainJava("src").end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").testJava("tests")
				.testRuntimeDeps(rtBinTool, rtSrcTool).end();
		wsRootHasDirectory("mod");

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();

		es.mutate(seCtx);

		assertDotClasspathContains("mod",
				"<classpathentry kind=\"lib\" path=\"" + cacheDir
						+ "/rtBinTool\" sourcepath=\"" + cacheDir
						+ "/rtBinTool-src\"/>");
		assertDotClasspathContains(
				"mod",
				"<classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/rtSrcTool\"/>");
	}

}
