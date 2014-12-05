package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.plugin.javamodules.JavaModules;

public class IwantModules extends JavaModules {

	@Override
	protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
		return m.javaCompliance(JavaCompliance.JAVA_1_7)
				.mainJava("src/main/java").testJava("src/test/java");
	}

	SortedSet<JavaSrcModule> modulesForCoverage() {
		SortedSet<JavaSrcModule> mods = new TreeSet<>(allSrcModules());
		mods.remove(iwantExampleWsdef);
		mods.remove(iwantMockWsroot);
		mods.remove(iwantTutorialWsdefs);
		return mods;
	}

	private IwantSrcModuleSpex essentialModule(String subName) {
		String fullName = "iwant-" + subName;
		return srcModule(fullName).locationUnderWsRoot("essential/" + fullName);
	}

	private IwantSrcModuleSpex optionalModule(String subName) {
		String fullName = "iwant-" + subName;
		return srcModule(fullName).locationUnderWsRoot("optional/" + fullName);
	}

	private IwantSrcModuleSpex privateModule(String subName) {
		String fullName = "iwant-" + subName;
		return srcModule(fullName).locationUnderWsRoot("private/" + fullName);
	}

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private JavaModule ant = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/ant").name("ant")
					.version("1.9.4")).end();

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private JavaModule antLauncher = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/ant")
					.name("ant-launcher").version("1.9.4")).end();

	private JavaModule asm = JavaBinModule.providing(
			FromRepository.ibiblio().group("asm").name("asm").version("3.2"))
			.end();

	private JavaModule commonsIo = JavaBinModule.providing(
			FromRepository.ibiblio().group("org/apache/commons")
					.name("commons-io").version("1.3.2")).end();

	private JavaModule commonsMath = JavaBinModule.providing(
			FromRepository.ibiblio().group("commons-math").name("commons-math")
					.version("1.2")).end();

	private JavaModule guava = JavaBinModule.providing(
			FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava").version("18.0")).end();

	private JavaModule guavaTestlib = JavaBinModule
			.providing(
					FromRepository.repo1MavenOrg().group("com/google/guava")
							.name("guava-testlib").version("18.0"))
			.runtimeDeps(guava).end();

	private JavaModule jaxen = JavaBinModule.providing(
			FromRepository.ibiblio().group("jaxen").name("jaxen")
					.version("1.1.4")).end();

	private JavaModule junit = JavaBinModule.providing(
			FromRepository.ibiblio().group("junit").name("junit")
					.version("4.8.2")).end();

	// TODO document dependency to asm, jaxen
	private JavaModule pmd = JavaBinModule.providing(
			FromRepository.ibiblio().group("pmd").name("pmd").version("4.3"))
			.end();

	private JavaSrcModule iwantApiModel = essentialModule("api-model")
			.mainDeps().testDeps(junit).end();

	private JavaSrcModule iwantTestarea = privateModule("testarea")
			.noTestJava().mainDeps(junit).end();

	private JavaBinModule iwantWsRootMarker = JavaBinModule.providing(
			Source.underWsroot("essential/iwant-wsroot-marker")).end();

	private JavaSrcModule iwantIwantWsrootFinder = essentialModule(
			"iwant-wsroot-finder").mainDeps(iwantWsRootMarker)
			.testDeps(commonsIo, junit).end();

	private JavaSrcModule iwantEntry = essentialModule("entry").noMainJava()
			.mainJava("as-some-developer/with/java").noTestJava().mainDeps()
			.end();

	private JavaSrcModule iwantEntrymocks = privateModule("entrymocks")
			.noTestJava().mainDeps(iwantEntry, iwantTestarea).end();

	private JavaSrcModule iwantEntryTests = privateModule("entry-tests")
			.noMainJava()
			.testResources("src/test/resources")
			.testDeps(guava, guavaTestlib, iwantEntry, iwantEntrymocks,
					iwantIwantWsrootFinder, iwantTestarea, junit).end();

	private JavaSrcModule iwantEntry2 = essentialModule("entry2")
			.mainDeps(iwantEntry)
			.testDeps(iwantEntrymocks, iwantIwantWsrootFinder, iwantTestarea,
					junit).end();

	private JavaSrcModule iwantCoreservices = essentialModule("coreservices")
			.mainDeps(iwantApiModel, iwantEntry, iwantEntry2)
			.testDeps(iwantTestarea, junit).end();

	private JavaSrcModule iwantApimocks = privateModule("apimocks")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry,
					iwantEntrymocks, iwantTestarea, junit).noTestJava().end();

	private JavaSrcModule iwantApiCore = essentialModule("api-core")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry)
			.testDeps(iwantApimocks, junit).end();

	private JavaSrcModule iwantApiJavamodules = essentialModule(
			"api-javamodules")
			.mainDeps(iwantApiCore, iwantApiModel, iwantEntry)
			.testDeps(iwantApimocks, iwantCoreservices, iwantTestarea, guava,
					guavaTestlib, junit).end();

	private JavaSrcModule iwantApiWsdef = essentialModule("api-wsdef")
			.noTestJava().mainDeps(iwantApiModel, iwantApiJavamodules).end();

	private JavaSrcModule iwantCoreDownload = essentialModule("core-download")
			.mainDeps(iwantApiCore, iwantApiModel, iwantCoreservices,
					iwantEntry).testDeps(iwantApimocks, iwantTestarea, junit)
			.end();

	private JavaSrcModule iwantPlannerApi = essentialModule("planner-api")
			.mainDeps(iwantEntry).testDeps(junit).end();

	private JavaSrcModule iwantPlannerMocks = essentialModule("planner-mocks")
			.noTestJava().mainDeps(iwantPlannerApi, junit).end();

	private JavaSrcModule iwantPlanner = essentialModule("planner")
			.mainDeps(iwantEntry, iwantPlannerApi)
			.testDeps(iwantPlannerMocks, junit).end();

	private JavaSrcModule iwantEclipseSettings = essentialModule(
			"eclipse-settings")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantEntry).testDeps(iwantApimocks, junit).end();

	private JavaSrcModule iwantEntry3 = essentialModule("entry3")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiWsdef, iwantCoreDownload, iwantCoreservices,
					iwantEntry, iwantEntry2, iwantIwantWsrootFinder,
					iwantPlanner, iwantPlannerApi)
			.testDeps(iwantApimocks, iwantEclipseSettings, iwantEntrymocks,
					iwantPlannerMocks, iwantTestarea, junit).end();

	private JavaSrcModule iwantEmbedded = essentialModule("embedded")
			.mainDeps(iwantApiModel, iwantApiJavamodules, iwantApiWsdef,
					iwantCoreservices, iwantEntry, iwantEntry3)
			.testDeps(iwantApimocks, iwantApiCore, iwantTestarea, junit).end();

	private JavaSrcModule iwantCoreAnt = essentialModule("core-ant")
			.mainDeps(iwantApiCore, iwantApiModel, iwantCoreservices,
					iwantEntry)
			.testDeps(iwantApimocks, iwantCoreDownload, iwantEmbedded,
					iwantTestarea, junit).end();

	private JavaSrcModule iwantDeprecatedEmma = essentialModule(
			"deprecated-emma")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantCoreAnt, iwantCoreservices, iwantEntry, iwantEntry3)
			.testDeps(iwantApimocks, iwantCoreDownload, iwantEmbedded, junit)
			.end();

	private JavaSrcModule iwantDocs = privateModule("docs").noMainJava()
			.noTestJava().end();

	private JavaSrcModule iwantExampleWsdef = essentialModule("example-wsdef")
			.noTestJava()
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiWsdef, iwantEntry3, iwantEclipseSettings).end();

	final JavaSrcModule iwantMockWsroot = withMockrootMainJavas(
			privateModule("mock-wsroot")).noTestJava().mainDeps(junit).end();

	private static IwantSrcModuleSpex withMockrootMainJavas(
			IwantSrcModuleSpex mod) {
		mod.noMainJava();
		mod.mainJava("essential/iwant-api-javamodules/src/main/java");
		mod.mainJava("essential/iwant-apimocks/src/main/java");
		mod.mainJava("essential/iwant-api-model/src/main/java");
		mod.mainJava("essential/iwant-api-wsdef/src/main/java");
		mod.mainJava("essential/iwant-coreservices/src/main/java");
		mod.mainJava("essential/iwant-entry/as-some-developer/with/java");
		mod.mainJava("essential/iwant-entry2/src/main/java");
		mod.mainJava("essential/iwant-entry3/src/main/java");
		mod.mainJava("essential/iwant-iwant-wsroot-finder/src/main/java");
		return mod;
	}

	private JavaSrcModule iwantTestresources = privateModule("testresources")
			.noTestJava().mainResources("src/main/resources").end();

	private JavaSrcModule iwantPluginAnt = optionalModule("plugin-ant")
			.testResources("src/test/resources")
			.mainDeps(ant, antLauncher, iwantApiCore, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantTestarea,
					iwantTestresources).end();

	private JavaSrcModule iwantPluginFindbugs = optionalModule(
			"plugin-findbugs")
			.testResources("src/test/resources")
			.mainDeps(commonsIo, iwantApiCore, iwantApiJavamodules,
					iwantApiModel, iwantCoreAnt, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantEmbedded,
					iwantTestarea).end();

	private JavaSrcModule iwantPluginGithub = optionalModule("plugin-github")
			.mainDeps(iwantApiCore, iwantApiModel, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt).testDeps(junit).end();

	private JavaSrcModule iwantPluginJacoco = optionalModule("plugin-jacoco")
			.mainDeps(commonsIo, iwantApiCore, iwantApiModel,
					iwantApiJavamodules, iwantCoreAnt, iwantCoreDownload,
					iwantEntry3, iwantPluginAnt)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantEmbedded,
					iwantTestarea).end();

	private JavaSrcModule iwantPluginJavamodules = optionalModule(
			"plugin-javamodules")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantCoreDownload, iwantPluginAnt).testDeps(junit).end();

	// TODO don't depend directly on asm, jaxen: pmd depends on them
	private JavaSrcModule iwantPluginPmd = optionalModule("plugin-pmd")
			.testResources("src/test/resources")
			.mainDeps(ant, asm, commonsIo, iwantApiCore, iwantApiModel,
					iwantEntry, jaxen, pmd)
			.testDeps(junit, iwantApimocks, iwantTestarea, iwantTestresources)
			.end();

	private JavaSrcModule iwantPluginWar = optionalModule("plugin-war")
			.mainDeps(ant, antLauncher, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantPluginAnt,
					iwantTestarea, iwantTestresources).end();

	private JavaSrcModule iwantTutorialWsdefs = privateModule("tutorial-wsdefs")
			.noMainJava()
			.noTestJava()
			.mainJava("src")
			.mainDeps(commonsMath, iwantApiCore, iwantApiJavamodules,
					iwantApiModel, iwantApiWsdef, iwantCoreAnt,
					iwantCoreDownload, iwantEntry3, iwantEclipseSettings,
					iwantPluginAnt, iwantPluginFindbugs, iwantPluginGithub,
					iwantPluginJacoco, iwantPluginJavamodules, iwantPluginPmd,
					iwantPluginWar, junit).end();

	/**
	 * Just for documenting, to help detect dead stuff
	 */
	@SuppressWarnings("unused")
	private final List<JavaSrcModule> modulesNotDependedByOthers = Arrays
			.asList(iwantDeprecatedEmma, iwantDocs, iwantEntryTests);

}
