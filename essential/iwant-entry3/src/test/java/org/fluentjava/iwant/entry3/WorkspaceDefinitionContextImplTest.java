package org.fluentjava.iwant.entry3;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.testarea.TestArea;

import junit.framework.TestCase;

public class WorkspaceDefinitionContextImplTest extends TestCase {

	private Set<JavaModule> apiModules;
	private WorkspaceModuleContext ctx;
	private JavaBinModule iwantApiModule1;
	private JavaBinModule iwantApiModule2;
	private JavaBinModule wsdefdefModule;
	private TestArea testArea;
	private File cachedIwantSrcRoot;

	@Override
	protected void setUp() throws Exception {
		iwantApiModule1 = JavaBinModule
				.providing(Source.underWsroot("iwant-api-1")).end();
		iwantApiModule2 = JavaBinModule
				.providing(Source.underWsroot("iwant-api-2")).end();
		wsdefdefModule = JavaBinModule.providing(Source.underWsroot("wsdefdef"))
				.end();

		apiModules = new LinkedHashSet<>();
		apiModules.add(iwantApiModule1);
		apiModules.add(iwantApiModule2);

		testArea = TestArea.forTest(this);
		cachedIwantSrcRoot = testArea.newDir("iwant-src");
		ctx = new WorkspaceDefinitionContextImpl(apiModules, cachedIwantSrcRoot,
				wsdefdefModule);
	}

	public void testApiModulesIsGivenAsSuch() {
		assertSame(apiModules, ctx.iwantApiModules());
	}

	public void testWsdefdefModuleIsGivenAsSuch() {
		assertSame(wsdefdefModule, ctx.wsdefdefModule());
	}

	public void testIwantPluginAntWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		assertEquals(5, mods.size());
		Iterator<JavaModule> iterator = mods.iterator();

		assertEquals("iwant-plugin-ant", iterator.next().name());
		assertSame(iwantApiModule1, iterator.next());
		assertSame(iwantApiModule2, iterator.next());
		assertEquals("ant-1.10.14.jar", iterator.next().name());
		assertEquals("ant-launcher-1.10.14.jar", iterator.next().name());
	}

	public void testIwantPluginAntMainClassesHasCorrectCompilationClasspath() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		JavaClasses classes = (JavaClasses) antPlugin.mainArtifact();
		Iterator<? extends Path> iterator = classes.classLocations().iterator();

		assertSame(iwantApiModule1.mainArtifact(), iterator.next());
		assertSame(iwantApiModule2.mainArtifact(), iterator.next());
		assertEquals("ant-1.10.14.jar", iterator.next().name());
	}

	public void testIwantPluginAntMainJavaIsASubdirectoryUnderIwantSources() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		JavaClasses classes = (JavaClasses) antPlugin.mainArtifact();
		assertEquals(1, classes.srcDirs().size());

		ExternalSource java = (ExternalSource) classes.srcDirs().iterator()
				.next();
		assertEquals(
				cachedIwantSrcRoot + "/optional/iwant-plugin-ant/src/main/java",
				java.name());
	}

	public void testIwantPluginPmdWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().pmd().withDependencies();

		assertEquals("[iwant-plugin-pmd, iwant-api-1, iwant-api-2,"
				+ " ant-1.10.14.jar, asm-3.2.jar, commons-io-1.3.2.jar,"
				+ " jaxen-1.1.4.jar, pmd-4.3.jar]", mods.toString());
	}

	public void testIwantPluginFindbugsWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().findbugs().withDependencies();

		assertEquals("[iwant-plugin-findbugs, iwant-api-1, iwant-api-2,"
				+ " commons-io-1.3.2.jar, iwant-plugin-ant, ant-1.10.14.jar,"
				+ " ant-launcher-1.10.14.jar]", mods.toString());
	}

	public void testIwantPluginGithubWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().github().withDependencies();

		assertEquals("[iwant-plugin-github, iwant-api-1, iwant-api-2,"
				+ " iwant-plugin-ant, ant-1.10.14.jar,"
				+ " ant-launcher-1.10.14.jar]", mods.toString());
	}

	public void testIwantPluginWarWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().war().withDependencies();

		assertEquals(
				"[iwant-plugin-war, iwant-api-1, iwant-api-2, ant-1.10.14.jar]",
				mods.toString());
	}

	public void testIwantPluginJacocoWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().jacoco().withDependencies();

		assertEquals(
				"[iwant-plugin-jacoco, iwant-api-1, iwant-api-2,"
						+ " iwant-plugin-ant, ant-1.10.14.jar,"
						+ " ant-launcher-1.10.14.jar, commons-io-1.3.2.jar]",
				mods.toString());
	}

	public void testIwantPluginTestngWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().testng().withDependencies();

		assertEquals(
				"[iwant-plugin-testng, iwant-api-1, iwant-api-2,"
						+ " jcommander-1.82.jar, testng-6.9.4.jar]",
				mods.toString());
	}

}
