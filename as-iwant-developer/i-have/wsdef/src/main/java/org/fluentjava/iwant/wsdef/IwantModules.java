package org.fluentjava.iwant.wsdef;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.javamodules.ScalaVersion;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.javamodules.JavaModules;

public class IwantModules extends JavaModules {

	private static final ScalaVersion SCALA_VER = ScalaVersion.of("2.12.19");

	@Override
	protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
		return m.javaCompliance(JavaCompliance.of("21"))
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
		return module(fullName).locationUnderWsRoot("essential/" + fullName);
	}

	private IwantSrcModuleSpex optionalModule(String subName) {
		String fullName = "iwant-" + subName;
		return module(fullName).locationUnderWsRoot("optional/" + fullName);
	}

	private IwantSrcModuleSpex privateModule(String subName) {
		String fullName = "iwant-" + subName;
		return module(fullName).locationUnderWsRoot("private/" + fullName);
	}

	private IwantSrcModuleSpex module(String fullName) {
		return srcModule(fullName).javaCompliance(JavaCompliance.JAVA_1_8);
	}

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private JavaModule ant = binModule("org.apache.ant", "ant", "1.10.7");

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private JavaModule antLauncher = binModule("org.apache.ant", "ant-launcher",
			"1.10.7");

	private JavaModule asm = JavaBinModule.providing(FromRepository
			.repo1MavenOrg().group("asm").name("asm").version("3.2").jar())
			.end();

	private JavaModule commonsIo = JavaBinModule.providing(
			FromRepository.repo1MavenOrg().group("org/apache/commons")
					.name("commons-io").version("1.3.2").jar())
			.end();

	private JavaModule commonsMath = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("commons-math")
					.name("commons-math").version("1.2").jar())
			.end();

	private JavaModule guava = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava").version("18.0").jar())
			.end();

	private JavaModule guavaTestlib = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava-testlib").version("18.0").jar())
			.runtimeDeps(guava).end();

	private JavaModule jaxen = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("jaxen")
					.name("jaxen").version("1.1.4").jar())
			.end();

	private JavaModule junit = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("junit")
					.name("junit").version("4.8.2").jar())
			.end();

	// TODO document dependency to asm, jaxen
	private JavaModule pmd = JavaBinModule.providing(FromRepository
			.repo1MavenOrg().group("pmd").name("pmd").version("4.3").jar())
			.end();

	private final JavaBinModule jcommander = binModule("com.beust",
			"jcommander", "1.48");

	private final JavaBinModule testng = binModule("org.testng", "testng",
			"6.9.4", jcommander);

	final JavaBinModule scalaLibrary = binModule("org/scala-lang",
			"scala-library", SCALA_VER.value());

	private static final String VERTX_VER = "3.4.2";
	private final JavaBinModule vertxCore = binModule("io.vertx", "vertx-core",
			VERTX_VER);
	private final JavaBinModule vertxWeb = binModule("io.vertx", "vertx-web",
			VERTX_VER);

	private static JavaBinModule nettyModule(String name) {
		return binModule("io.netty", "netty-" + name, "4.1.75.Final");
	}

	private final JavaBinModule nettyBuffer = nettyModule("buffer");
	private final JavaBinModule nettyCodec = nettyModule("codec");
	private final JavaBinModule nettyCodecDns = nettyModule("codec-dns");
	private final JavaBinModule nettyCodecHttp = nettyModule("codec-http");
	private final JavaBinModule nettyCodecHttp2 = nettyModule("codec-http2");
	private final JavaBinModule nettyCommon = nettyModule("common");
	private final JavaBinModule nettyResolver = nettyModule("resolver");
	private final JavaBinModule nettyResolverDns = nettyModule("resolver-dns");
	private final JavaBinModule nettyTransport = nettyModule("transport");
	private final JavaBinModule nettyHandler = nettyModule("handler");
	private final JavaBinModule nettyHandlerProxy = nettyModule(
			"handler-proxy");

	private JavaSrcModule iwantApiModel = essentialModule("api-model")
			.mainDeps().testDeps(junit).end();

	private JavaBinModule iwantWsRootMarker = JavaBinModule
			.providing(Source.underWsroot("essential/iwant-wsroot-marker"))
			.end();

	private JavaSrcModule iwantIwantWsrootFinder = essentialModule(
			"iwant-wsroot-finder").mainDeps(iwantWsRootMarker)
					.testDeps(commonsIo, junit).end();

	private JavaSrcModule iwantEntry = essentialModule("entry").noMainJava()
			.mainJava("as-some-developer/with/java").noTestJava().mainDeps()
			.end();

	private JavaSrcModule iwantTestarea = privateModule("testarea").noTestJava()
			.mainDeps(iwantEntry, junit).end();

	private JavaSrcModule iwantEntrymocks = privateModule("entrymocks")
			.mainDeps(iwantEntry, iwantTestarea).testDeps(junit).end();

	private JavaSrcModule iwantEntry2 = essentialModule("entry2")
			.mainDeps(iwantEntry).testDeps(iwantEntrymocks,
					iwantIwantWsrootFinder, iwantTestarea, junit)
			.end();

	private JavaSrcModule iwantCoreservices = essentialModule("coreservices")
			.mainDeps(iwantApiModel, iwantEntry, iwantEntry2)
			.testDeps(iwantTestarea, junit).end();

	private JavaSrcModule iwantApimocks = privateModule("apimocks")
			.mainDeps(iwantApiModel, iwantCoreservices, iwantEntry,
					iwantEntrymocks, iwantTestarea, junit)
			.noTestJava().end();

	private JavaSrcModule iwantApiTarget = essentialModule("api-target")
			.mainDeps(iwantApiModel).testDeps(junit).end();

	private JavaSrcModule iwantApiCore = essentialModule("api-core")
			.mainDeps(iwantApiModel, iwantApiTarget, iwantCoreservices,
					iwantEntry)
			.testDeps(iwantApimocks, junit).end();

	private JavaSrcModule iwantApiAntrunner = essentialModule("api-antrunner")
			.noMainResources().noTestResources().mainDeps(ant, iwantEntry)
			.testDeps(junit).end();

	private JavaSrcModule iwantCoreDownload = essentialModule("core-download")
			.mainDeps(iwantApiCore, iwantApiModel, iwantApiTarget,
					iwantCoreservices, iwantEntry, iwantEntry2)
			.testDeps(iwantApimocks, iwantTestarea, junit).end();

	private JavaSrcModule iwantApiZip = essentialModule("api-zip")
			.testResources("src/test/resources").mainDeps(ant, antLauncher,
					iwantApiCore, iwantApiModel, iwantApiTarget)
			.testDeps(iwantApimocks, junit).end();

	private JavaSrcModule iwantApiJavamodules = essentialModule(
			"api-javamodules")
					.mainDeps(iwantApiAntrunner, iwantApiCore, iwantApiModel,
							iwantApiTarget, iwantApiZip, iwantCoreDownload,
							iwantCoreservices, iwantEntry)
					.testDeps(iwantApimocks, iwantTestarea, guava, guavaTestlib,
							junit)
					.end();

	private JavaSrcModule iwantApiWsdef = essentialModule("api-wsdef")
			.noTestJava().mainDeps(iwantApiModel, iwantApiJavamodules).end();

	private JavaSrcModule iwantCoreJavamodules = essentialModule(
			"core-javamodules")
					.mainDeps(iwantApiJavamodules, iwantApiModel,
							iwantApiTarget, iwantApiZip, iwantCoreDownload)
					.testDeps(junit).end();

	private JavaSrcModule iwantApiBash = essentialModule("api-bash")
			.mainResources("src/main/resources")
			.testResources("src/test/resources")
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiTarget, iwantApiWsdef, iwantCoreservices,
					iwantEntry)
			.testDeps(iwantApimocks, junit).end();

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
							iwantApiTarget, iwantEntry)
					.testDeps(iwantApimocks, junit).end();

	private JavaSrcModule iwantCoreJavafinder = essentialModule(
			"core-javafinder")
					.mainDeps(iwantApiJavamodules, iwantApiModel, iwantApiWsdef,
							iwantEntry)
					.testDeps(junit).end();

	private JavaSrcModule iwantEntry3 = essentialModule("entry3")
			.mainDeps(iwantApiBash, iwantApiCore, iwantApiJavamodules,
					iwantApiModel, iwantApiTarget, iwantApiWsdef,
					iwantCoreDownload, iwantCoreJavafinder, iwantCoreservices,
					iwantEntry, iwantEntry2, iwantIwantWsrootFinder,
					iwantPlanner, iwantPlannerApi)
			.testDeps(iwantApimocks, iwantEclipseSettings, iwantEntrymocks,
					iwantPlannerMocks, iwantTestarea, junit)
			.end();

	private JavaSrcModule iwantEmbedded = essentialModule("embedded")
			.mainDeps(iwantApiModel, iwantApiJavamodules, iwantApiTarget,
					iwantApiWsdef, iwantCoreservices, iwantEntry, iwantEntry3)
			.testDeps(iwantApimocks, iwantApiCore, iwantTestarea, junit).end();

	private JavaSrcModule iwantCoreAnt = essentialModule("core-ant")
			.mainDeps(iwantApiAntrunner, iwantApiCore, iwantApiModel,
					iwantApiTarget, iwantCoreservices, iwantEntry)
			.testDeps(iwantApimocks, iwantCoreDownload, iwantEmbedded,
					iwantTestarea, junit)
			.end();

	private JavaSrcModule iwantDocs = privateModule("docs").noMainJava()
			.noTestJava().end();

	private JavaSrcModule iwantExampleWsdef = essentialModule("example-wsdef")
			.noTestJava()
			.mainDeps(iwantApiCore, iwantApiJavamodules, iwantApiModel,
					iwantApiTarget, iwantApiWsdef, iwantEntry3,
					iwantEclipseSettings)
			.end();

	final JavaSrcModule iwantMockWsroot = withMockrootMainJavas(
			privateModule("mock-wsroot")).noTestJava().mainDeps(junit).end();

	private static IwantSrcModuleSpex withMockrootMainJavas(
			IwantSrcModuleSpex mod) {
		mod.noMainJava();
		mod.mainJava("essential/iwant-api-antrunner/src/main/java");
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
			.mainDeps(ant, antLauncher, iwantApiCore, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantEntry, iwantTestarea,
					iwantTestresources)
			.end();

	private JavaSrcModule iwantPluginFindbugs = optionalModule(
			"plugin-findbugs")
					.testResources("src/test/resources")
					.mainDeps(commonsIo, iwantApiAntrunner, iwantApiCore,
							iwantApiJavamodules, iwantApiModel, iwantApiTarget,
							iwantCoreAnt, iwantCoreDownload, iwantEntry,
							iwantEntry3, iwantPluginAnt)
					.testDeps(junit, iwantApimocks, iwantEmbedded,
							iwantTestarea)
					.end();

	private JavaSrcModule iwantPluginGithub = optionalModule("plugin-github")
			.mainDeps(iwantApiCore, iwantApiModel, iwantApiTarget, iwantApiZip,
					iwantCoreDownload, iwantEntry3, iwantPluginAnt)
			.testDeps(junit).end();

	private JavaSrcModule iwantPluginJacoco = optionalModule("plugin-jacoco")
			.mainDeps(commonsIo, iwantApiAntrunner, iwantApiCore, iwantApiModel,
					iwantApiJavamodules, iwantApiTarget, iwantApiZip,
					iwantCoreAnt, iwantCoreDownload, iwantEntry, iwantEntry3,
					iwantPluginAnt)
			.testDeps(junit, iwantApimocks, iwantEmbedded, iwantTestarea).end();

	// TODO don't depend directly on asm, jaxen: pmd depends on them
	private JavaSrcModule iwantPluginPmd = optionalModule("plugin-pmd")
			.testResources("src/test/resources")
			.mainDeps(ant, asm, commonsIo, iwantApiCore, iwantApiModel,
					iwantApiTarget, iwantEntry, jaxen, pmd)
			.testDeps(junit, iwantApimocks, iwantTestarea, iwantTestresources)
			.end();

	private JavaSrcModule iwantPluginTestng = optionalModule("plugin-testng")
			.mainDeps(iwantApiJavamodules, testng).testDeps(iwantEntry, junit)
			.end();

	private JavaSrcModule iwantPluginWar = optionalModule("plugin-war")
			.mainDeps(ant, antLauncher, iwantApiModel)
			.testDeps(junit, iwantApimocks, iwantApiZip, iwantEntry,
					iwantPluginAnt, iwantTestarea, iwantTestresources)
			.end();

	private JavaSrcModule iwantTutorialWsdefs = privateModule("tutorial-wsdefs")
			.scalaVersion(SCALA_VER).noMainJava().noTestJava().mainJava("src")
			.mainDeps(commonsMath, iwantApiBash, iwantApiCore,
					iwantApiJavamodules, iwantApiModel, iwantApiTarget,
					iwantApiWsdef, iwantApiZip, iwantCoreAnt, iwantCoreDownload,
					iwantCoreJavamodules, iwantCoreservices,
					iwantCoreJavafinder, iwantEntry3, iwantEclipseSettings,
					iwantPluginAnt, iwantPluginFindbugs, iwantPluginGithub,
					iwantPluginJacoco, iwantPluginPmd, iwantPluginTestng,
					iwantPluginWar, junit, scalaLibrary, testng)
			.end();

	private final Target extendedIwantEnumsJava = new ExtendedIwantEnums(
			"extended-iwant-enums");

	private final Target extendedIwantEnumsClasses = JavaClasses.with()
			.name(extendedIwantEnumsJava.name() + "-classes")
			.srcDirs(extendedIwantEnumsJava).end();

	private final JavaModule iwantExtendedEnums = JavaBinModule
			.providing(extendedIwantEnumsClasses, extendedIwantEnumsJava).end();

	private JavaSrcModule iwantEntryTests = privateModule("entry-tests")
			.noMainJava().testResources("src/test/resources")
			.testDeps(commonsIo, guava, guavaTestlib, iwantApiCore,
					iwantApiModel, iwantApiTarget, iwantEntry, iwantEntrymocks,
					iwantIwantWsrootFinder, iwantTestarea, junit, nettyBuffer,
					nettyCodec, nettyCodecDns, nettyCodecHttp, nettyCodecHttp2,
					nettyCommon, nettyHandler, nettyHandlerProxy, nettyResolver,
					nettyResolverDns, nettyTransport, vertxCore, vertxWeb)
			.end();

	private final JavaSrcModule iwantTests = privateModule("tests").noMainJava()
			.testResources("src/test/resources").testDeps(iwantExtendedEnums)
			.testDeps(allSrcModules()).testDeps(commonsIo, junit).end();

	/**
	 * Just for documenting, to help detect dead stuff
	 */
	@SuppressWarnings("unused")
	private List<JavaSrcModule> modulesNotDependedByOthers() {
		List<JavaSrcModule> m = new ArrayList<>();

		// internal:
		m.add(iwantDocs);
		m.add(iwantEntryTests);
		m.add(iwantTests);
		return m;
	}

}
