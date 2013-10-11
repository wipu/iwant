package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.StandardCharacteristics.ProductionCode;
import net.sf.iwant.api.javamodules.StandardCharacteristics.ProductionConfiguration;
import net.sf.iwant.api.javamodules.StandardCharacteristics.ProductionRuntimeData;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.eclipsesettings.EclipseSettingsTestArea;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class JavaBinModuleTest extends TestCase {

	private TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private CachesMock caches;
	private File cachedTargets;
	private TargetEvaluationContextMock evCtx;

	@Override
	protected void setUp() throws Exception {
		testArea = new EclipseSettingsTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		cachedTargets = testArea.newDir("cached-targets");
		caches.cachesModifiableTargetsAt(cachedTargets);
		evCtx = new TargetEvaluationContextMock(iwant, caches);
	}

	// provided by src module

	public void testToStringOfProjectProvidedBinIsTheName() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaModule lib = JavaBinModule.named("lib.jar").inside(libsModule);

		assertEquals("lib.jar", lib.toString());
	}

	public void testMainArtifactOfBinModuleIsTheJarAsSource() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaModule lib = JavaBinModule.named("lib.jar").inside(libsModule);

		Source artifact = (Source) lib.mainArtifact();
		assertEquals("libs/lib.jar", artifact.name());
	}

	public void testEclipsePathsOfBinInsideLibProject() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("lib.jar").inside(libsModule);

		assertEquals("/libs/lib.jar", lib.eclipseBinaryReference(evCtx));
		assertEquals(null, lib.eclipseSourceReference(evCtx));
	}

	public void testEclipsePathsOfBinInsideLibProjectAndWithSources() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("lib2.jar")
				.source("lib2-src.zip").inside(libsModule);

		assertEquals("/libs/lib2.jar", lib.eclipseBinaryReference(evCtx));
		assertEquals("/libs/lib2-src.zip", lib.eclipseSourceReference(evCtx));
	}

	public void testSourceOfBinInsideProjectWhenGivenAndWhenNot() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule lib = JavaBinModule.named("sourced.jar")
				.source("sourced-src.zip").inside(libsModule);

		Source src = (Source) lib.source();
		assertEquals("libs/sourced-src.zip", src.name());

		assertNull(JavaBinModule.named("unsourced.jar").inside(libsModule)
				.source());
	}

	public void testCharacteristicsForBinaryModuleInsideLibraryModule() {
		JavaSrcModule libsModule = JavaSrcModule.with().name("libs").end();
		JavaBinModule bin = JavaBinModule.named("mod.jar")
				.has(ProductionCode.class).inside(libsModule);

		assertEquals(
				"[interface net.sf.iwant.api.javamodules.StandardCharacteristics$ProductionCode]",
				bin.characteristics().toString());
		assertTrue(bin.doesHave(ProductionRuntimeData.class));
	}

	// path provider module

	public void testToStringOfPathProviderBinIsTheName() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, null)
				.end();

		assertEquals("lib.jar", libJarModule.toString());
	}

	public void testBinModuleThatProvidesAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, null)
				.end();

		assertEquals("lib.jar", libJarModule.name());
		assertSame(libJar, libJarModule.mainArtifact());
	}

	public void testEclipsePathsOfModuleThatProvidesAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar).end();

		assertEquals(cachedTargets + "/lib.jar",
				libJarModule.eclipseBinaryReference(evCtx));
		assertEquals(null, libJarModule.eclipseSourceReference(evCtx));
	}

	public void testEclipsePathsOfModuleThatProvidesAMainArtifactTargetWithSources() {
		Target libJar = new HelloTarget("lib.jar", "");
		Target libSrc = new HelloTarget("lib-src.zip", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar, libSrc)
				.end();

		assertEquals(cachedTargets + "/lib.jar",
				libJarModule.eclipseBinaryReference(evCtx));
		assertEquals(cachedTargets + "/lib-src.zip",
				libJarModule.eclipseSourceReference(evCtx));
	}

	public void testBinaryModulesDontHaveMainDeps() {
		assertTrue(JavaBinModule.named("lib")
				.inside(JavaSrcModule.with().name("libs").end()).mainDeps()
				.isEmpty());
		assertTrue(JavaBinModule.providing(Source.underWsroot("lib")).end()
				.mainDeps().isEmpty());
	}

	public void testSourcesOfProviderModule() {
		Source src = Source.underWsroot("src");

		assertSame(src, JavaBinModule.providing(Source.underWsroot("bin"), src)
				.end().source());
	}

	public void testCharacteristicsOfProviderModule() {
		JavaBinModule mod = JavaBinModule
				.providing(new HelloTarget("lib.jar", ""), null)
				.has(ProductionConfiguration.class).end();

		assertEquals(
				"[interface net.sf.iwant.api.javamodules.StandardCharacteristics$ProductionConfiguration]",
				mod.characteristics().toString());
		assertTrue(mod.doesHave(ProductionRuntimeData.class));
	}

}
