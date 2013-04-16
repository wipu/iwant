package net.sf.iwant.entry3;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;

public class WorkspaceDefinitionContextImplTest extends TestCase {

	private Set<JavaModule> apiModules;
	private File iwantWs;
	private WorkspaceDefinitionContext ctx;
	private JavaBinModule iwantApiModule1;
	private JavaBinModule iwantApiModule2;

	@Override
	protected void setUp() throws Exception {
		iwantApiModule1 = JavaBinModule.providing(Source
				.underWsroot("iwant-api-1"));
		iwantApiModule2 = JavaBinModule.providing(Source
				.underWsroot("iwant-api-2"));

		apiModules = new LinkedHashSet<JavaModule>();
		apiModules.add(iwantApiModule1);
		apiModules.add(iwantApiModule2);

		iwantWs = new File("iwantWs");
		ctx = new WorkspaceDefinitionContextImpl(apiModules, iwantWs);
	}

	public void testApiModulesIsGivenAsSuch() {
		assertSame(apiModules, ctx.iwantApiModules());
	}

	public void testIwantPluginAntWithDependenciesContainsCorrectModules() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		assertEquals(4, mods.size());
		Iterator<JavaModule> iterator = mods.iterator();

		assertEquals("iwant-plugin-ant", iterator.next().name());
		assertSame(iwantApiModule1, iterator.next());
		assertSame(iwantApiModule2, iterator.next());
		assertEquals("ant-1.7.1", iterator.next().name());
	}

	public void testIwantPluginMainClassesHasCorrectCompilationClasspath() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		JavaClasses classes = (JavaClasses) antPlugin.mainArtifact();
		Iterator<? extends Path> iterator = classes.classLocations().iterator();

		assertSame(iwantApiModule1.mainArtifact(), iterator.next());
		assertSame(iwantApiModule2.mainArtifact(), iterator.next());
		assertEquals("ant-1.7.1", iterator.next().name());
	}

	public void testIwantPluginContainsSources() {
		Set<JavaModule> mods = ctx.iwantPlugin().ant().withDependencies();

		JavaBinModule antPlugin = (JavaBinModule) mods.iterator().next();
		ExternalSource src = (ExternalSource) antPlugin.source();

		assertEquals(new File(iwantWs, "iwant-plugin-ant/src/main/java"),
				src.file());
	}

}
