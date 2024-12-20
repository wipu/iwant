package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.KotlinVersion;
import org.fluentjava.iwant.api.javamodules.ScalaVersion;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.junit.jupiter.api.Test;

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

	@Test
	public void modulesCanBeAddedInManyPartsBothAsVarargsAndAsCollections() {
		JavaSrcModule m1 = JavaSrcModule.with().name("m1").end();
		JavaSrcModule m2 = JavaSrcModule.with().name("m2").end();
		JavaSrcModule m3 = JavaSrcModule.with().name("m3").end();
		JavaSrcModule m4 = JavaSrcModule.with().name("m4").end();
		JavaSrcModule m5 = JavaSrcModule.with().name("m5").end();
		EclipseSettings es = EclipseSettings.with().modules(m1, m2)
				.modules(Arrays.asList(m3, m4)).modules(m5).end();

		assertEquals("[m1, m2, m3, m4, m5]", es.modules().toString());
	}

	@Test
	public void modulesAreCollectedToSortedSetSoMultipleAddsAndWrongOrderDontAffectIt() {
		JavaSrcModule m1 = JavaSrcModule.with().name("m1").end();
		JavaSrcModule m2 = JavaSrcModule.with().name("m2").end();
		JavaSrcModule m3 = JavaSrcModule.with().name("m3").end();
		JavaSrcModule m4 = JavaSrcModule.with().name("m4").end();
		JavaSrcModule m5 = JavaSrcModule.with().name("m5").end();
		EclipseSettings es = EclipseSettings.with().modules(m5, m4)
				.modules(Arrays.asList(m3)).modules(m1, m2, m3, m4, m5).end();

		assertEquals("[m1, m2, m3, m4, m5]", es.modules().toString());
	}

	@Test
	public void mutationUsingWsdefdefAndWsdefAndAnotherModuleUsedByWsdef() {
		JavaModule iwantClasses = JavaBinModule
				.providing(TargetMock.ingredientless("iwant-classes"),
						TargetMock.ingredientless("combined-iwant-sources"))
				.end();
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
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/iwant-classes\" sourcepath=\"" + slashed(cached)
						+ "/combined-iwant-sources\"/>");

		assertDotClasspathContains("utils/wsdef-tools",
				"<classpathentry kind=\"src\" path=\"src\"/>");

		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"src\" path=\"src/main/java\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/iwant-classes\" sourcepath=\"" + slashed(cached)
						+ "/combined-iwant-sources\"/>");
		assertDotClasspathContains("as-someone/i-have/wsdef",
				"<classpathentry combineaccessrules=\"false\""
						+ " kind=\"src\" path=\"/test-wsdef-tools\"/>");
	}

	@Test
	public void noSourcesMeansNoSources() {
		JavaModule srcless = JavaSrcModule.with().name("any")
				.locationUnderWsRoot("any").end();
		wsRootHasDirectory("any");

		EclipseSettings es = EclipseSettings.with().modules(srcless).name("es")
				.end();

		es.mutate(seCtx);

		assertFalse(contentOfFileUnderWsRoot("any/.classpath")
				.contains("<classpathentry kind=\"src\" "));
	}

	@Test
	public void testJavaAndTestDepsAffectDotClasspath() {
		JavaModule testTools1 = JavaBinModule
				.providing(TargetMock.ingredientless("test-tools-1"),
						TargetMock.ingredientless("test-tools-1-src"))
				.end();
		JavaModule testTools2 = JavaBinModule.providing(
				TargetMock.ingredientless("test-tools-2-srcless"), null).end();

		JavaModule mod1 = JavaSrcModule.with().name("mod1")
				.locationUnderWsRoot("mod1").mainJava("src").testJava("tests1")
				.testDeps(testTools1).end();
		JavaModule mod2 = JavaSrcModule.with().name("mod2")
				.locationUnderWsRoot("mod2").mainJava("src2").testJava("tests2")
				.testDeps(testTools2).end();
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
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/test-tools-1\" sourcepath=\"" + slashed(cached)
						+ "/test-tools-1-src\"/>");
		assertDotClasspathContains("mod2",
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/test-tools-2-srcless\"/>");
	}

	@Test
	public void setOfAllMainAndTestDependencyBinaryClassesAndSourcesGetsRefreshedSoEclipseWontComplainAboutMissingRefs() {
		// 2 modules depend on this, but it's mentioned only once in refs:
		JavaModule binWithoutSources = JavaBinModule
				.providing(TargetMock.ingredientless("binWithoutSources"))
				.end();
		// sources of this shall also be refreshed, they are dynamic:
		JavaModule binWithSources = JavaBinModule
				.providing(TargetMock.ingredientless("binWithSources"),
						TargetMock.ingredientless("binWithSources-src"))
				.end();
		// source dep shall not be refreshed, eclipse can do that by itself:
		JavaSrcModule srcDep = JavaSrcModule.with().name("srcDep")
				.locationUnderWsRoot("srcDep").mainJava("src")
				.mainDeps(binWithoutSources).end();
		// not only main, but test deps also:
		JavaModule testUtilWithSources = JavaBinModule
				.providing(TargetMock.ingredientless("testUtilWithSources"),
						TargetMock.ingredientless("testUtilWithSources-src"))
				.end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").testJava("test")
				.mainDeps(binWithoutSources, binWithSources, srcDep)
				.testDeps(testUtilWithSources).end();

		EclipseSettings es = EclipseSettings.with().modules(mod, srcDep)
				.name("es").end();
		es.mutate(seCtx);

		assertEquals(1, seCtx.targetsWantedAsPath().size());
		assertEquals("org.fluentjava.iwant.api.core.Concatenated\n"
				+ "i:native-path:\n" + "  testUtilWithSources\n" + "p:string:\n"
				+ "  \\n\n" + "i:native-path:\n" + "  testUtilWithSources-src\n"
				+ "p:string:\n" + "  \\n\n" + "i:native-path:\n"
				+ "  binWithoutSources\n" + "p:string:\n" + "  \\n\n"
				+ "i:native-path:\n" + "  binWithSources\n" + "p:string:\n"
				+ "  \\n\n" + "i:native-path:\n" + "  binWithSources-src\n"
				+ "p:string:\n" + "  \\n\n" + "",
				seCtx.targetsWantedAsPath().get(0).contentDescriptor());
	}

	@Test
	public void classpathIsGeneratedButWithWarningIfRefreshOfDepsFails() {
		JavaModule util = JavaBinModule
				.providing(TargetMock.ingredientless("util"),
						TargetMock.ingredientless("util-src"))
				.end();
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").mainDeps(util)
				.end();

		seCtx.shallFailIwantAsPathWith(
				new RuntimeException("compilation of util classes failed"));
		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		assertEquals(1, seCtx.targetsWantedAsPath().size());
		assertEquals(
				"org.fluentjava.iwant.api.core.Concatenated\n"
						+ "i:native-path:\n" + "  util\n" + "p:string:\n"
						+ "  \\n\n" + "i:native-path:\n" + "  util-src\n"
						+ "p:string:\n" + "  \\n\n" + "",
				seCtx.targetsWantedAsPath().get(0).contentDescriptor());

		assertDotClasspathContains("mod",
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/util\" sourcepath=\"" + slashed(cached)
						+ "/util-src\"/>");
		assertEquals("WARNING: Refresh of eclipse settings references failed:\n"
				+ "java.lang.RuntimeException: compilation of util classes failed\n"
				+ "", seCtx.err().toString());
	}

	@Test
	public void nameOfBinDepRefreshTarget() {
		JavaModule util = JavaBinModule
				.providing(TargetMock.ingredientless("util"),
						TargetMock.ingredientless("util-src"))
				.end();
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

	@Test
	public void defaultJavaComplianceIs11() {
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		String corePrefs = "mod/.settings/org.eclipse.jdt.core.prefs";
		assertFileContains(corePrefs, "targetPlatform=11");
		assertFileContains(corePrefs, "compiler.compliance=11");
		assertFileContains(corePrefs, "compiler.source=11");
	}

	@Test
	public void javaComplianceCanBeDefinedAs17() {
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

	@Test
	public void javaAndResourceDirsAreCreated() {
		JavaModule modWithAll = JavaSrcModule.with().name("mod-with-all")
				.locationUnderWsRoot("mod-with-all").mavenLayout().end();
		JavaModule modWithNone = JavaSrcModule.with().name("mod-with-none")
				.locationUnderWsRoot("mod-with-all").end();
		JavaBinModule binToBeExcluded = JavaBinModule
				.providing(Source.underWsroot("bin")).end();

		EclipseSettings es = EclipseSettings.with()
				.modules(modWithAll, modWithNone, binToBeExcluded).name("es")
				.end();
		es.mutate(seCtx);

		assertFalse(new File(wsRoot, "mod-with-none/src/main/java").exists());
		assertFalse(
				new File(wsRoot, "mod-with-none/src/main/resources").exists());
		assertFalse(new File(wsRoot, "mod-with-none/src/test/java").exists());
		assertFalse(
				new File(wsRoot, "mod-with-none/src/test/resources").exists());

		assertTrue(new File(wsRoot, "mod-with-all/src/main/java").exists());
		assertTrue(
				new File(wsRoot, "mod-with-all/src/main/resources").exists());
		assertTrue(new File(wsRoot, "mod-with-all/src/test/java").exists());
		assertTrue(
				new File(wsRoot, "mod-with-all/src/test/resources").exists());
	}

	@Test
	public void testRuntimeDepenencyGoesToDotClasspath() {
		JavaModule rtBinTool = JavaBinModule
				.providing(TargetMock.ingredientless("rtBinTool"),
						TargetMock.ingredientless("rtBinTool-src"))
				.end();

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
				"<classpathentry kind=\"lib\" path=\"" + slashed(cached)
						+ "/rtBinTool\" sourcepath=\"" + slashed(cached)
						+ "/rtBinTool-src\"/>");
		assertDotClasspathContains("mod",
				"<classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/rtSrcTool\"/>");
	}

	@Test
	public void dotProjectHasScalaSupportIfEnabledByTheModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mixed")
				.scalaVersion(ScalaVersion._2_11_7()).mainJava("src/main/java")
				.mainScala("src/main/scala").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();

		es.mutate(seCtx);

		assertDotProjectContains("mixed",
				"<name>org.scala-ide.sdt.core.scalabuilder</name>");
		assertDotProjectContains("mixed",
				"<nature>org.scala-ide.sdt.core.scalanature</nature>");
	}

	@Test
	public void dotProjectHasKotlinSupportIfEnabledByTheModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod")
				.kotlinVersion(KotlinVersion._1_3_60())
				.mainJava("src/main/java").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();

		es.mutate(seCtx);

		assertDotProjectContains("mod",
				"<name>org.jetbrains.kotlin.ui.kotlinBuilder</name>");
		assertDotProjectContains("mod",
				"<nature>org.jetbrains.kotlin.core.kotlinNature</nature>");
	}

	@Test
	public void dotClasspathHasKotlinContainerIfEnabledByTheModule() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod")
				.kotlinVersion(KotlinVersion._1_3_60())
				.mainJava("src/main/java").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();

		es.mutate(seCtx);

		assertDotClasspathContains("mod",
				"<classpathentry kind=\"con\" path=\"org.jetbrains.kotlin.core.KOTLIN_CONTAINER\"/>");
	}

	@Test
	public void kotlinPreferencesExistIfAndOnlyIfModuleHasKotlinSupport() {
		JavaModule withKotlin = JavaSrcModule.with().name("with-kotlin")
				.kotlinVersion(KotlinVersion._1_3_60()).mainJava("src").end();
		JavaModule withoutKotlin = JavaSrcModule.with().name("without-kotlin")
				.mainJava("src").end();

		EclipseSettings es = EclipseSettings.with()
				.modules(withKotlin, withoutKotlin).name("es").end();
		es.mutate(seCtx);

		String kotlinPrefs = "with-kotlin/.settings/org.jetbrains.kotlin.core.prefs";
		assertFileContains(kotlinPrefs,
				"codeStyle/codeStyleId=KOTLIN_OFFICIAL");

		assertFalse(new File(wsRoot,
				"without-kotlin/.settings/org.jetbrains.kotlin.core.prefs")
						.exists());
	}

	@Test
	public void encodingIsUtf8() {
		JavaModule mod = JavaSrcModule.with().name("mod")
				.locationUnderWsRoot("mod").mainJava("src").end();

		EclipseSettings es = EclipseSettings.with().modules(mod).name("es")
				.end();
		es.mutate(seCtx);

		String resourcesPrefs = "mod/.settings/org.eclipse.core.resources.prefs";
		assertFileContains(resourcesPrefs, "=UTF-8");
	}

}
