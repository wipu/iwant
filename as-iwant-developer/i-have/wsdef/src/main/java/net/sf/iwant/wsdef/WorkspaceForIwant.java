package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.StringFilter;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.deprecated.emma.EmmaTargetsOfJavaModules;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.findbugs.FindbugsDistribution;
import net.sf.iwant.plugin.findbugs.FindbugsOutputFormat;
import net.sf.iwant.plugin.findbugs.FindbugsReport;
import net.sf.iwant.plugin.jacoco.JacocoDistribution;
import net.sf.iwant.plugin.jacoco.JacocoTargetsOfJavaModules;

public class WorkspaceForIwant implements IwantWorkspace {

	private static final StringFilter testClassNameFilter = new TestClassNameFilter();

	private final FindbugsDistribution findbugs = FindbugsDistribution
			.ofVersion("2.0.3");

	private static final Target copyOfLocalIwantWs = new CopyOfLocalIwantWsForTutorial();

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(copyOfLocalIwantWs, emmaCoverageReport(),
				findbugsReport(), jacocoReport(), listOfExternalDeps(),
				localWebsite(), remoteWebsite());
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(allModules()).end());
	}

	private static IwantSrcModuleSpex essentialModule(String subName) {
		String fullName = "iwant-" + subName;
		return JavaSrcModule.with().name(fullName)
				.locationUnderWsRoot("essential/" + fullName)
				.mainJava("src/main/java").testJava("src/test/java")
				.testedBy(testClassNameFilter);
	}

	private static IwantSrcModuleSpex optionalModule(String subName) {
		String fullName = "iwant-" + subName;
		return JavaSrcModule.with().name(fullName)
				.locationUnderWsRoot("optional/" + fullName)
				.mainJava("src/main/java").testJava("src/test/java")
				.testedBy(testClassNameFilter);
	}

	private static IwantSrcModuleSpex privateModule(String subName) {
		String fullName = "iwant-" + subName;
		return JavaSrcModule.with().name(fullName)
				.locationUnderWsRoot("private/" + fullName)
				.mainJava("src/main/java").testJava("src/test/java")
				.testedBy(testClassNameFilter);
	}

	private static SortedSet<JavaSrcModule> allSrcModules() {
		return new TreeSet<JavaSrcModule>(Arrays.asList(iwantApiCore,
				iwantApiJavamodules, iwantApimocks, iwantApiModel,
				iwantApiWsdef, iwantCoreAnt, iwantCoreDownload,
				iwantCoreservices, iwantDeprecatedEmma, iwantEntry,
				iwantEntrymocks, iwantEntryTests, iwantEntry2, iwantEntry3,
				iwantDocs, iwantEclipseSettings, iwantEmbedded,
				iwantExampleWsdef, iwantIwantWsrootFinder, iwantMockWsroot(),
				iwantPlanner, iwantPlannerApi, iwantPlannerMocks,
				iwantPluginAnt, iwantPluginFindbugs, iwantPluginGithub,
				iwantPluginJacoco, iwantPluginPmd, iwantPluginWar,
				iwantTestarea, iwantTestresources, iwantTutorialWsdefs));
	}

	private static SortedSet<JavaSrcModule> modulesForCoverage() {
		SortedSet<JavaSrcModule> mods = allSrcModules();
		mods.remove(iwantExampleWsdef);
		mods.remove(iwantMockWsroot());
		mods.remove(iwantTutorialWsdefs);
		return mods;
	}

	private static SortedSet<JavaModule> allModules() {
		SortedSet<JavaModule> all = new TreeSet<JavaModule>();
		all.addAll(allSrcModules());
		all.addAll(Arrays.asList(ant, commonsMath, junit));
		return all;
	}

	private static class TestClassNameFilter implements StringFilter {

		@Override
		public boolean matches(String candidate) {
			return candidate.matches(".*Test$")
					&& !candidate.matches(".*Abstract[^.]*Test$");
		}

		@Override
		public String toString() {
			return getClass().getCanonicalName();
		}

	}

	// the targets

	private static Path emma() {
		return TestedIwantDependencies.emma();
	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static Target jacocoReport() {
		return JacocoTargetsOfJavaModules
				.with()
				.jacocoWithDeps(jacoco(), asm501Jar)
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modules(modulesForCoverage()).end()
				.jacocoReport("jacoco-report");

	}

	private static Target emmaCoverageReport() {
		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules
				.with()
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar()).emma(emma())
				.modules(allModules()).butNotInstrumenting(iwantMockWsroot())
				.filter(emmaFilter()).end();
		return emmaTargets.emmaReport("emma-coverage");

	}

	private static Path emmaFilter() {
		ConcatenatedBuilder filter = Concatenated.named("emma-filter");
		// only used in the tutorial, not "real" code:
		filter.string("-com.example.*\n");
		return filter.end();
	}

	private Target findbugsReport() {
		return findbugsReport("findbugs-report", allSrcModules(),
				FindbugsOutputFormat.HTML);

	}

	private FindbugsReport findbugsReport(String name,
			Collection<JavaSrcModule> modules, FindbugsOutputFormat outputFormat) {
		return FindbugsReport
				.with()
				.name(name)
				.outputFormat(outputFormat)
				.using(findbugs, TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modulesToAnalyze(modules).end();
	}

	private static Target listOfExternalDeps() {
		ConcatenatedBuilder deps = Concatenated.named("list-of-ext-deps");
		deps.pathTo(ant.mainArtifact()).string("\n");
		deps.pathTo(antLauncher.mainArtifact()).string("\n");
		deps.pathTo(asm.mainArtifact()).string("\n");
		deps.pathTo(commonsIo.mainArtifact()).string("\n");
		deps.pathTo(commonsMath.mainArtifact()).string("\n");
		deps.pathTo(jaxen.mainArtifact()).string("\n");
		deps.pathTo(junit.mainArtifact()).string("\n");
		deps.pathTo(pmd.mainArtifact()).string("\n");
		return deps.end();
	}

	private static Target localTutorial() {
		return Tutorial.local(copyOfLocalIwantWs);
	}

	private static Target localWebsite() {
		return new Website("local-website", localTutorial());
	}

	private static Target remoteTutorial() {
		return Tutorial.remote();
	}

	private static Target remoteWebsite() {
		return new Website("remote-website", remoteTutorial());
	}

	// the modules

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private static JavaModule ant = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/ant").name("ant")
					.version("1.7.1")).end();

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private static JavaModule antLauncher = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/ant")
					.name("ant-launcher").version("1.7.1")).end();

	private static JavaModule asm = JavaBinModule.providing(
			FromRepository.ibiblio().group("asm").name("asm").version("3.2"))
			.end();

	private static Path asm501Jar = FromRepository.repo1MavenOrg()
			.group("org/ow2/asm").name("asm-all").version("5.0.1");

	private static JavaModule commonsIo = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/commons")
					.name("commons-io").version("1.3.2")).end();

	private static JavaModule commonsMath = JavaBinModule.providing(
			FromRepository.ibiblio().group("commons-math").name("commons-math")
					.version("1.2")).end();

	private static JavaModule guava = JavaBinModule.providing(
			FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava").version("18.0")).end();

	private static JavaModule guavaTestlib = JavaBinModule
			.providing(
					FromRepository.repo1MavenOrg().group("com/google/guava")
							.name("guava-testlib").version("18.0"))
			.runtimeDeps(guava).end();

	private static JavaModule jaxen = JavaBinModule.providing(
			FromRepository.ibiblio().group("jaxen").name("jaxen")
					.version("1.1.4")).end();

	private static JavaModule junit = JavaBinModule.providing(
			FromRepository.ibiblio().group("junit").name("junit")
					.version("4.8.2")).end();

	// TODO document dependency to asm, jaxen
	private static JavaModule pmd = JavaBinModule.providing(
			FromRepository.ibiblio().group("pmd").name("pmd").version("4.3"))
			.end();

	private static JavaSrcModule iwantApiModel = essentialModule("api-model")
			.mainDeps().testDeps(junit).end();

	private static JavaSrcModule iwantTestarea = privateModule("testarea")
			.noTestJava().mainDeps(junit).end();

	private static JavaBinModule iwantWsRootMarker = JavaBinModule.providing(
			Source.underWsroot("essential/iwant-wsroot-marker")).end();

	private static JavaSrcModule iwantIwantWsrootFinder = essentialModule(
			"iwant-wsroot-finder").mainDeps(iwantWsRootMarker)
			.testDeps(commonsIo, junit).end();

	private static JavaSrcModule iwantEntry = essentialModule("entry")
			.noMainJava().mainJava("as-some-developer/with/java").noTestJava()
			.mainDeps().end();

	private static JavaSrcModule iwantEntrymocks = privateModule("entrymocks")
			.noTestJava().mainDeps(iwantEntry, iwantTestarea).end();

	private static JavaSrcModule iwantEntryTests = privateModule("entry-tests")
			.noMainJava()
			.testResources("src/test/resources")
			.testDeps(guava, guavaTestlib, iwantEntry, iwantEntrymocks,
					iwantIwantWsrootFinder, iwantTestarea, junit).end();

	private static JavaSrcModule iwantEntry2 = essentialModule("entry2")
			.mainDeps(iwantEntry)
			.testDeps(iwantEntrymocks, iwantIwantWsrootFinder, iwantTestarea,
					junit).end();

	private static JavaSrcModule iwantCoreservices = essentialModule(
			"coreservices").mainDeps(iwantApiModel, iwantEntry, iwantEntry2)
			.testDeps(iwantTestarea, junit).end();

	private static JavaSrcModule iwantApimocks = privateModule("apimocks")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry,
					iwantEntrymocks, iwantTestarea, junit).noTestJava().end();

	private static JavaSrcModule iwantApiCore = essentialModule("api-core")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry)
			.testDeps(iwantApimocks, junit).end();

	private static JavaSrcModule iwantApiJavamodules = essentialModule(
			"api-javamodules")
			.mainDeps(iwantApiCore, iwantApiModel)
			.testDeps(iwantApimocks, iwantCoreservices, iwantEntry,
					iwantTestarea, junit).end();

	private static JavaSrcModule iwantApiWsdef = essentialModule("api-wsdef")
			.noTestJava().mainDeps(iwantApiModel, iwantApiJavamodules).end();

	private static JavaSrcModule iwantCoreDownload = essentialModule(
			"core-download")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry)
			.testDeps(iwantApimocks, iwantTestarea, junit).end();

	private static JavaSrcModule iwantPlannerApi = essentialModule(
			"planner-api").mainDeps(iwantEntry).testDeps(junit).end();

	private static JavaSrcModule iwantPlannerMocks = essentialModule(
			"planner-mocks").noTestJava().mainDeps(iwantPlannerApi, junit)
			.end();

	private static JavaSrcModule iwantPlanner = essentialModule("planner")
			.mainDeps(iwantEntry, iwantPlannerApi)
			.testDeps(iwantPlannerMocks, junit).end();

	private static JavaSrcModule iwantEclipseSettings = essentialModule(
			"eclipse-settings")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantEntry).testDeps(iwantApimocks, junit).end();

	private static JavaSrcModule iwantEntry3 = essentialModule("entry3")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiWsdef, iwantCoreDownload, iwantCoreservices,
					iwantEntry, iwantEntry2, iwantIwantWsrootFinder,
					iwantPlanner, iwantPlannerApi)
			.testDeps(iwantApimocks, iwantEclipseSettings, iwantEntrymocks,
					iwantPlannerMocks, iwantTestarea, junit).end();

	private static JavaSrcModule iwantEmbedded = essentialModule("embedded")
			.mainDeps(iwantApiModel, iwantApiJavamodules, iwantApiWsdef,
					iwantCoreservices, iwantEntry, iwantEntry3)
			.testDeps(iwantApimocks, iwantApiCore, iwantTestarea, junit).end();

	private static JavaSrcModule iwantCoreAnt = essentialModule("core-ant")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry)
			.testDeps(iwantApiCore, iwantApimocks, iwantCoreDownload,
					iwantEmbedded, iwantTestarea, junit).end();

	private static JavaSrcModule iwantDeprecatedEmma = essentialModule(
			"deprecated-emma")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantCoreAnt, iwantCoreservices, iwantEntry, iwantEntry3)
			.testDeps(iwantApimocks, iwantCoreDownload, iwantEmbedded, junit)
			.end();

	private static JavaSrcModule iwantDocs = privateModule("docs").noMainJava()
			.noTestJava().end();

	private static JavaSrcModule iwantExampleWsdef = essentialModule(
			"example-wsdef")
			.noTestJava()
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiWsdef, iwantEntry3, iwantEclipseSettings).end();

	private static JavaSrcModule iwantMockWsroot() {
		IwantSrcModuleSpex mod = privateModule("mock-wsroot").noMainJava()
				.noTestJava();
		mod.mainJava("essential/iwant-api-javamodules/src/main/java");
		mod.mainJava("essential/iwant-apimocks/src/main/java");
		mod.mainJava("essential/iwant-api-model/src/main/java");
		mod.mainJava("essential/iwant-api-wsdef/src/main/java");
		mod.mainJava("essential/iwant-coreservices/src/main/java");
		mod.mainJava("essential/iwant-entry/as-some-developer/with/java");
		mod.mainJava("essential/iwant-entry2/src/main/java");
		mod.mainJava("essential/iwant-entry3/src/main/java");
		mod.mainJava("essential/iwant-iwant-wsroot-finder/src/main/java");
		return mod.mainDeps(junit).end();
	}

	private static JavaSrcModule iwantTestresources = privateModule(
			"testresources").noTestJava().mainResources("src/main/resources")
			.end();

	private static JavaSrcModule iwantPluginAnt = optionalModule("plugin-ant")
			.testResources("src/test/resources")
			.mainDeps(ant, antLauncher, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantTestarea,
					iwantTestresources).end();

	private static JavaSrcModule iwantPluginFindbugs = optionalModule(
			"plugin-findbugs")
			.testResources("src/test/resources")
			.mainDeps(commonsIo, iwantApiCore, iwantApiJavamodules,
					iwantApiModel, iwantCoreAnt, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantEmbedded,
					iwantTestarea).end();

	private static JavaSrcModule iwantPluginGithub = optionalModule(
			"plugin-github")
			.mainDeps(iwantApiCore, iwantApiModel, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt).testDeps(junit).end();

	private static JavaSrcModule iwantPluginJacoco = optionalModule(
			"plugin-jacoco")
			.mainDeps(commonsIo, iwantApiCore, iwantApiModel,
					iwantApiJavamodules, iwantCoreAnt, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantEmbedded,
					iwantTestarea).end();

	// TODO don't depend directly on asm, jaxen: pmd depends on them
	private static JavaSrcModule iwantPluginPmd = optionalModule("plugin-pmd")
			.testResources("src/test/resources")
			.mainDeps(ant, asm, commonsIo, iwantApiModel, jaxen, pmd)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantTestarea,
					iwantTestresources).end();

	private static JavaSrcModule iwantPluginWar = optionalModule("plugin-war")
			.mainDeps(ant, antLauncher, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantPluginAnt,
					iwantTestarea, iwantTestresources).end();

	private static JavaSrcModule iwantTutorialWsdefs = privateModule(
			"tutorial-wsdefs")
			.noMainJava()
			.noTestJava()
			.mainJava("src")
			.mainDeps(commonsMath, iwantApiCore, iwantApiJavamodules,
					iwantApiModel, iwantApiWsdef, iwantCoreAnt,
					iwantCoreDownload, iwantEntry3, iwantEclipseSettings,
					iwantPluginAnt, iwantPluginFindbugs, iwantPluginGithub,
					iwantPluginJacoco, iwantPluginPmd, iwantPluginWar).end();

}
