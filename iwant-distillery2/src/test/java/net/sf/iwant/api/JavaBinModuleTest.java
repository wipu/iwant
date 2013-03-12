package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.eclipsesettings.EclipseSettingsTestArea;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
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

	// path provider module

	public void testBinModuleThatProvidesAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar);

		assertEquals("lib.jar", libJarModule.name());
		assertSame(libJar, libJarModule.mainArtifact());
	}

	public void testEclipsePathsOfModuleThatProviderAMainArtifactTarget() {
		Target libJar = new HelloTarget("lib.jar", "");
		JavaBinModule libJarModule = JavaBinModule.providing(libJar);

		assertEquals(cachedTargets + "/lib.jar",
				libJarModule.eclipseBinaryReference(evCtx));
		assertEquals(null, libJarModule.eclipseSourceReference(evCtx));
	}

}
