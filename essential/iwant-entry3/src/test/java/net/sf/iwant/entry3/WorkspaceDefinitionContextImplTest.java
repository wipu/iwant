package net.sf.iwant.entry3;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.wsdef.WorkspaceDefinitionContext;

public class WorkspaceDefinitionContextImplTest extends TestCase {

	private Set<JavaModule> apiModules;
	private File iwantWs;
	private WorkspaceDefinitionContext ctx;
	private JavaBinModule iwantApiModule1;
	private JavaBinModule iwantApiModule2;
	private JavaBinModule wsdefdefModule;

	@Override
	protected void setUp() throws Exception {
		iwantApiModule1 = JavaBinModule.providing(
				Source.underWsroot("iwant-api-1")).end();
		iwantApiModule2 = JavaBinModule.providing(
				Source.underWsroot("iwant-api-2")).end();
		wsdefdefModule = JavaBinModule
				.providing(Source.underWsroot("wsdefdef")).end();

		apiModules = new LinkedHashSet<JavaModule>();
		apiModules.add(iwantApiModule1);
		apiModules.add(iwantApiModule2);

		iwantWs = new File("iwantWs");
		ctx = new WorkspaceDefinitionContextImpl(apiModules, iwantWs,
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
		assertEquals("ant-1.7.1.jar", iterator.next().name());
		assertEquals("ant-launcher-1.7.1.jar", iterator.next().name());
	}

	public void testIwantPluginAntMainClassesHasCorrectCompilationClasspath() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		JavaClasses classes = (JavaClasses) antPlugin.mainArtifact();
		Iterator<? extends Path> iterator = classes.classLocations().iterator();

		assertSame(iwantApiModule1.mainArtifact(), iterator.next());
		assertSame(iwantApiModule2.mainArtifact(), iterator.next());
		assertEquals("ant-1.7.1.jar", iterator.next().name());
	}

	public void testIwantPluginAntContainsSources() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		ExternalSource src = (ExternalSource) antPlugin.source();

		assertEquals(new File(iwantWs, "iwant-plugin-ant/src/main/java"),
				src.file());
	}

	public void testIwantPluginPmdWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().pmd().withDependencies();

		assertEquals("[iwant-plugin-pmd, iwant-api-1, iwant-api-2,"
				+ " ant-1.7.1.jar, asm-3.2.jar, commons-io-1.3.2.jar,"
				+ " jaxen-1.1.4.jar, pmd-4.3.jar]", mods.toString());
	}

	public void testIwantPluginFindbugsWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().findbugs().withDependencies();

		assertEquals("[iwant-plugin-findbugs, iwant-api-1, iwant-api-2,"
				+ " commons-io-1.3.2.jar, iwant-plugin-ant, ant-1.7.1.jar,"
				+ " ant-launcher-1.7.1.jar]", mods.toString());
	}

	public void testIwantPluginGithubWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().github().withDependencies();

		assertEquals("[iwant-plugin-github, iwant-api-1, iwant-api-2,"
				+ " iwant-plugin-ant, ant-1.7.1.jar,"
				+ " ant-launcher-1.7.1.jar]", mods.toString());
	}

	public void testIwantPluginWarWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().war().withDependencies();

		assertEquals(
				"[iwant-plugin-war, iwant-api-1, iwant-api-2, ant-1.7.1.jar]",
				mods.toString());
	}

	public void testIwantPluginJacocoWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().jacoco().withDependencies();

		assertEquals("[iwant-plugin-jacoco, iwant-api-1, iwant-api-2,"
				+ " iwant-plugin-ant, ant-1.7.1.jar,"
				+ " ant-launcher-1.7.1.jar, commons-io-1.3.2.jar]",
				mods.toString());
	}

}
