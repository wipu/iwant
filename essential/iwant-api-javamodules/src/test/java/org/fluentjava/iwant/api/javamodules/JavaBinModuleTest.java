package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionCode;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionConfiguration;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.ProductionRuntimeData;
import org.fluentjava.iwant.api.javamodules.StandardCharacteristics.TestUtility;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class JavaBinModuleTest extends IwantTestCase {

	// provided by src module

	@Test
	public void toStringOfProjectProvidedBinIsTheName() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaModule lib = JavaBinModule.named("lib.jar").inside(libsModule)
				.end();

		assertEquals("lib.jar", lib.toString());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void equalsUsesNameAndClass() {
		assertTrue(JavaBinModule.providing(Source.underWsroot("a")).end()
				.equals(JavaBinModule.providing(Source.underWsroot("a"))
						.end()));

		assertFalse(JavaBinModule.providing(Source.underWsroot("a")).end()
				.equals(JavaSrcModule.with().name("a").end()));
		assertFalse(JavaBinModule.providing(Source.underWsroot("a")).end()
				.equals(JavaBinModule.providing(Source.underWsroot("b"))
						.end()));
	}

	@Test
	public void hashCodeIsSameIfNameIsSame() {
		assertTrue(JavaBinModule.providing(Source.underWsroot("a")).end()
				.hashCode() == JavaBinModule.providing(Source.underWsroot("a"))
						.end().hashCode());
		assertTrue(JavaBinModule.providing(Source.underWsroot("a")).end()
				.hashCode() == JavaSrcModule.with().name("a").end().hashCode());
	}

	@Test
	public void mainArtifactOfBinModuleIsTheJarAsSource() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaModule lib = JavaBinModule.named("lib.jar").inside(libsModule)
				.end();

		Source artifact = (Source) lib.mainArtifact();
		assertEquals("libs/lib.jar", artifact.name());
	}

	@Test
	public void eclipsePathsOfBinInsideLibProject() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("lib.jar").inside(libsModule)
				.end();

		assertEquals("/libs/lib.jar", lib.eclipseBinaryReference(ctx));
		assertEquals(null, lib.eclipseSourceReference(ctx));
	}

	@Test
	public void eclipsePathsOfBinInsideLibProjectAndWithSources() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("lib2.jar")
				.source("lib2-src.zip").inside(libsModule).end();

		assertEquals("/libs/lib2.jar", lib.eclipseBinaryReference(ctx));
		assertEquals("/libs/lib2-src.zip", lib.eclipseSourceReference(ctx));
	}

	@Test
	public void sourceOfBinInsideProjectWhenGivenAndWhenNot() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("sourced.jar")
				.source("sourced-src.zip").inside(libsModule).end();

		Source src = (Source) lib.source();
		assertEquals("libs/sourced-src.zip", src.name());

		assertNull(JavaBinModule.named("unsourced.jar").inside(libsModule).end()
				.source());
	}

	@Test
	public void characteristicsForBinaryModuleInsideLibraryModule() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule bin = JavaBinModule.named("mod.jar")
				.has(ProductionCode.class).inside(libsModule).end();

		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$ProductionCode]",
				bin.characteristics().toString());
		assertTrue(bin.doesHave(ProductionRuntimeData.class));
	}

	@Test
	public void observableDepsOfBinInsideLibraryModuleThatHasRuntimeDeps() {
		JavaBinModule dep1 = JavaBinModule
				.providing(Source.underWsroot("dep1.jar")).end();
		JavaBinModule dep2 = JavaBinModule
				.providing(Source.underWsroot("dep2.jar")).end();

		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("lib.jar")
				.runtimeDeps(dep2, dep1, dep1).inside(libsModule).end();

		assertEquals("[]", lib.mainDepsForCompilation().toString());
		assertEquals("[dep2.jar, dep1.jar]",
				lib.mainDepsForRunOnly().toString());
		assertEquals("[lib.jar, dep2.jar, dep1.jar]",
				lib.effectivePathForMainRuntime().toString());
		assertEquals("[]",
				lib.testDepsForCompilationExcludingMainDeps().toString());
		assertEquals("[]",
				lib.testDepsForRunOnlyExcludingMainDeps().toString());
		assertEquals("[]", lib.effectivePathForTestCompile().toString());
		// bin module itself is not tested so no effective deps either:
		assertEquals("[]", lib.effectivePathForTestRuntime().toString());
	}

	@Test
	public void moduleLikeABinUnderLibsIsReallyLikeIt() {
		JavaSrcModule libs = JavaSrcModule.with().name("libs").end();
		JavaBinModule m1 = JavaBinModule.named("bin.jar").has(TestUtility.class)
				.runtimeDeps(JavaBinModule
						.providing(Source.underWsroot("runtime.jar")).end())
				.source("bin-src.zip").inside(libs).end();

		JavaBinModule m2 = JavaBinModule.likeBinUnderLibs(m1).end();

		assertEquals("bin.jar", m2.name());
		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$TestUtility]",
				m2.characteristics().toString());
		assertEquals("libs/bin.jar", m2.mainArtifact().toString());
		assertEquals("[runtime.jar]", m2.mainDepsForRunOnly().toString());
		assertEquals("libs/libs/bin-src.zip", m2.source().toString());
	}

	// path provider module

	@Test
	public void toStringOfPathProviderBinIsTheName() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, null)
				.end();

		assertEquals("lib.jar", libJarModule.toString());
	}

	@Test
	public void binModuleThatProvidesAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, null)
				.end();

		assertEquals("lib.jar", libJarModule.name());
		assertSame(libJar, libJarModule.mainArtifact());
	}

	@Test
	public void eclipsePathsOfModuleThatProvidesAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar).end();

		assertEquals(slashed(cached) + "/lib.jar",
				libJarModule.eclipseBinaryReference(ctx));
		assertEquals(null, libJarModule.eclipseSourceReference(ctx));
	}

	@Test
	public void eclipsePathsOfModuleThatProvidesAMainArtifactTargetWithSources() {
		Target libJar = new HelloTarget("lib.jar", "");
		Target libSrc = new HelloTarget("lib-src.zip", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, libSrc)
				.end();

		assertEquals(slashed(cached) + "/lib.jar",
				libJarModule.eclipseBinaryReference(ctx));
		assertEquals(slashed(cached) + "/lib-src.zip",
				libJarModule.eclipseSourceReference(ctx));
	}

	@Test
	public void binaryModulesDontHaveMainDepsForCompilation() {
		assertTrue(JavaBinModule.named("lib")
				.inside(JavaSrcModule.with().name("libs").end()).end()
				.mainDepsForCompilation().isEmpty());

		assertTrue(JavaBinModule.providing(Source.underWsroot("lib")).end()
				.mainDepsForCompilation().isEmpty());
	}

	@Test
	public void binaryModulesDontHaveTestDepsOfAnyKind() {
		JavaBinModule binInsideLibs = JavaBinModule.named("lib")
				.inside(JavaSrcModule.with().name("libs").end()).end();
		assertTrue(binInsideLibs.testDepsForCompilationExcludingMainDeps()
				.isEmpty());
		assertTrue(
				binInsideLibs.testDepsForRunOnlyExcludingMainDeps().isEmpty());
		assertEquals("[]",
				binInsideLibs.effectivePathForTestCompile().toString());
		assertEquals("[]",
				binInsideLibs.effectivePathForTestRuntime().toString());

		JavaBinModule binProvider = JavaBinModule
				.providing(Source.underWsroot("lib")).end();
		assertTrue(binProvider.testDepsForCompilationExcludingMainDeps()
				.isEmpty());
		assertTrue(binProvider.testDepsForRunOnlyExcludingMainDeps().isEmpty());
		assertEquals("[]",
				binInsideLibs.effectivePathForTestCompile().toString());
		assertEquals("[]",
				binInsideLibs.effectivePathForTestRuntime().toString());
	}

	@Test
	public void sourcesOfProviderModule() {
		Source src = Source.underWsroot("src");

		assertSame(src, JavaBinModule.providing(Source.underWsroot("bin"), src)
				.end().source());
	}

	@Test
	public void characteristicsOfProviderModule() {
		JavaBinModule mod = JavaBinModule
				.providing(new HelloTarget("lib.jar", ""), null)
				.has(ProductionConfiguration.class).end();

		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$ProductionConfiguration]",
				mod.characteristics().toString());
		assertTrue(mod.doesHave(ProductionRuntimeData.class));
	}

	@Test
	public void observableDepsOfPathProviderModuleThatHasRuntimeDeps() {
		JavaBinModule dep1 = JavaBinModule
				.providing(Source.underWsroot("dep1.jar")).end();
		JavaBinModule dep2 = JavaBinModule
				.providing(Source.underWsroot("dep2.jar")).end();

		JavaBinModule lib = JavaBinModule
				.providing(Source.underWsroot("lib.jar"))
				.runtimeDeps(dep2, dep1, dep1).end();

		assertEquals("[]", lib.mainDepsForCompilation().toString());
		assertEquals("[dep2.jar, dep1.jar]",
				lib.mainDepsForRunOnly().toString());
		assertEquals("[lib.jar, dep2.jar, dep1.jar]",
				lib.effectivePathForMainRuntime().toString());
		assertEquals("[]",
				lib.testDepsForCompilationExcludingMainDeps().toString());
		assertEquals("[]",
				lib.testDepsForRunOnlyExcludingMainDeps().toString());
		assertEquals("[]", lib.effectivePathForTestCompile().toString());
		// bin module itself is not tested so no effective deps either:
		assertEquals("[]", lib.effectivePathForTestRuntime().toString());
	}

	@Test
	public void moduleLikeAPathProviderIsReallyLikeIt() {
		JavaBinModule m1 = JavaBinModule
				.providing(Source.underWsroot("lib.jar"),
						Source.underWsroot("lib-src.zip"))
				.has(TestUtility.class)
				.runtimeDeps(JavaBinModule
						.providing(Source.underWsroot("runtime.jar")).end())
				.end();

		JavaBinModule m2 = JavaBinModule.likePathProvider(m1).end();

		assertEquals("lib.jar", m2.name());
		assertEquals(
				"[interface org.fluentjava.iwant.api.javamodules.StandardCharacteristics$TestUtility]",
				m2.characteristics().toString());
		assertEquals("lib.jar", m2.mainArtifact().toString());
		assertEquals("[runtime.jar]", m2.mainDepsForRunOnly().toString());
		assertEquals("lib-src.zip", m2.source().toString());
	}

}
