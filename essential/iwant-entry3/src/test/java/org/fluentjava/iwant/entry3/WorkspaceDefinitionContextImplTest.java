package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkspaceDefinitionContextImplTest {

	private Set<JavaModule> apiModules;
	private WorkspaceModuleContext ctx;
	private JavaBinModule iwantApiModule1;
	private JavaBinModule iwantApiModule2;
	private JavaBinModule wsdefdefModule;
	private TestArea testArea;
	private File cachedIwantSrcRoot;

	@BeforeEach
	protected void before() throws Exception {
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

	@Test
	public void apiModulesIsGivenAsSuch() {
		assertSame(apiModules, ctx.iwantApiModules());
	}

	@Test
	public void wsdefdefModuleIsGivenAsSuch() {
		assertSame(wsdefdefModule, ctx.wsdefdefModule());
	}

	@Test
	public void iwantPluginAntWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		assertEquals(5, mods.size());
		Iterator<JavaModule> iterator = mods.iterator();

		assertEquals("iwant-plugin-ant", iterator.next().name());
		assertSame(iwantApiModule1, iterator.next());
		assertSame(iwantApiModule2, iterator.next());
		assertEquals("ant-1.10.14.jar", iterator.next().name());
		assertEquals("ant-launcher-1.10.14.jar", iterator.next().name());
	}

	@Test
	public void iwantPluginAntMainClassesHasCorrectCompilationClasspath() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		JavaClasses classes = (JavaClasses) antPlugin.mainArtifact();
		Iterator<? extends Path> iterator = classes.classLocations().iterator();

		assertSame(iwantApiModule1.mainArtifact(), iterator.next());
		assertSame(iwantApiModule2.mainArtifact(), iterator.next());
		assertEquals("ant-1.10.14.jar", iterator.next().name());
	}

	@Test
	public void iwantPluginAntMainJavaIsASubdirectoryUnderIwantSources() {
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

	@Test
	public void iwantPluginPmdWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().pmd().withDependencies();

		assertEquals("[iwant-plugin-pmd, iwant-api-1, iwant-api-2,"
				+ " ant-1.10.14.jar, asm-3.2.jar, commons-io-1.3.2.jar,"
				+ " jaxen-1.1.4.jar, pmd-4.3.jar]", mods.toString());
	}

	@Test
	public void iwantPluginFindbugsWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().findbugs().withDependencies();

		assertEquals("[iwant-plugin-findbugs, iwant-api-1, iwant-api-2,"
				+ " commons-io-1.3.2.jar, iwant-plugin-ant, ant-1.10.14.jar,"
				+ " ant-launcher-1.10.14.jar]", mods.toString());
	}

	@Test
	public void iwantPluginGithubWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().github().withDependencies();

		assertEquals(
				"[iwant-plugin-github, iwant-plugin-ant,"
						+ " iwant-api-1, iwant-api-2,"
						+ " ant-1.10.14.jar, ant-launcher-1.10.14.jar]",
				mods.toString());
	}

	@Test
	public void iwantPluginWarWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().war().withDependencies();

		assertEquals(
				"[iwant-plugin-war, iwant-api-1, iwant-api-2, ant-1.10.14.jar]",
				mods.toString());
	}

	@Test
	public void iwantPluginJacocoWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().jacoco().withDependencies();

		assertEquals(
				"[iwant-plugin-jacoco, iwant-api-1, iwant-api-2,"
						+ " iwant-plugin-ant, ant-1.10.14.jar,"
						+ " ant-launcher-1.10.14.jar, commons-io-1.3.2.jar]",
				mods.toString());
	}

	@Test
	public void iwantPluginJunit5runnerWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().junit5runner()
				.withDependencies();

		assertEquals("[iwant-plugin-junit5runner,"
				+ " hamcrest-core-1.3.jar, junit-4.13.2.jar, junit-jupiter-5.10.2.jar,"
				+ " junit-jupiter-api-5.10.2.jar, junit-jupiter-engine-5.10.2.jar,"
				+ " junit-platform-commons-1.10.2.jar, junit-jupiter-params-5.10.2.jar,"
				+ " junit-platform-console-1.10.2.jar, junit-platform-launcher-1.10.2.jar,"
				+ " junit-platform-engine-1.10.2.jar, junit-vintage-engine-5.10.2.jar,"
				+ " opentest4j-1.3.0.jar]", mods.toString());
	}

	@Test
	public void iwantPluginTestngWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().testng().withDependencies();

		assertEquals(
				"[iwant-plugin-testng, iwant-api-1, iwant-api-2,"
						+ " jcommander-1.82.jar, testng-6.9.4.jar]",
				mods.toString());
	}

}
