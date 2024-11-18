package org.fluentjava.iwant.wsdef;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.download.GnvArtifact;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.core.javamodules.JavaModules;
import org.fluentjava.iwant.wsdefdef.WorkspaceProviderForIwant;

public class IwantModules extends JavaModules {

	private static final ScalaVersion SCALA_VER = ScalaVersion.of("2.12.3");
	private final WorkspaceContext ctx;

	public IwantModules(WorkspaceContext ctx) {
		this.ctx = ctx;
		// make sure all are defined, for example for eclipse-settings:
		modulesNotDependedByOthers();
	}

	@Override
	protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
		return m.javaCompliance(JavaCompliance.of("17"))
				.mainJava("src/main/java").testJava("src/test/java");
	}

	SortedSet<JavaSrcModule> modulesForCoverage() {
		SortedSet<JavaSrcModule> mods = new TreeSet<>(allSrcModules());
		mods.remove(iwantExampleWsdef());
		mods.remove(iwantMockWsroot());
		mods.remove(iwantTutorialWsdefs());
		return mods;
	}

	private IwantSrcModuleSpex essentialModule(String subName) {
		String fullName = "iwant-" + subName;
		return module(fullName).locationUnderWsRoot("essential/" + fullName)
				.testDeps(ctx.iwantPlugin().junit5runner().withDependencies());
	}

	private IwantSrcModuleSpex optionalModule(String subName) {
		String fullName = "iwant-" + subName;
		return module(fullName).locationUnderWsRoot("optional/" + fullName)
				.testDeps(ctx.iwantPlugin().junit5runner().withDependencies());
	}

	private IwantSrcModuleSpex privateModule(String subName) {
		String fullName = "iwant-" + subName;
		return module(fullName).locationUnderWsRoot("private/" + fullName)
				.testDeps(ctx.iwantPlugin().junit5runner().withDependencies());
	}

	private IwantSrcModuleSpex module(String fullName) {
		return srcModule(fullName).javaCompliance(JavaCompliance.JAVA_17);
	}

	private JavaModule ant = binModule(TestedIwantDependencies.antJar());

	private JavaModule antLauncher = binModule(
			TestedIwantDependencies.antLauncherJar());

	private JavaModule asm = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("org.ow2.asm")
					.name("asm").version("9.7").jar())
			.end();

	private JavaModule commonsIo = WorkspaceProviderForIwant.commonsIo();

	private JavaModule commonsLang3 = binModule("org.apache.commons",
			"commons-lang3", "3.16.0");

	private JavaModule commonsMath = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("commons-math")
					.name("commons-math").version("1.2").jar())
			.end();

	private static final String GUAVA_VER = "33.2.1-jre";

	private JavaModule guava = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava").version(GUAVA_VER).jar())
			.end();

	private JavaModule guavaTestlib = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("com/google/guava")
					.name("guava-testlib").version(GUAVA_VER).jar())
			.runtimeDeps(guava).end();

	private JavaModule jaxen = JavaBinModule
			.providing(FromRepository.repo1MavenOrg().group("jaxen")
					.name("jaxen").version("2.0.0").jar())
			.end();

	private static final Set<JavaModule> junitJupiterModules() {
		// TODO rm redundancy between this and WorkspaceDefinitionContextImpl
		Set<JavaModule> deps = new LinkedHashSet<>();
		for (GnvArtifact<Downloaded> dep : TestedIwantDependencies
				.junitJupiterCompileDeps()) {
			deps.add(JavaBinModule.providing(dep).end());
		}
		for (GnvArtifact<Downloaded> dep : TestedIwantDependencies
				.junitJupiterRtDeps()) {
			deps.add(JavaBinModule.providing(dep).end());
		}
		return deps;
	}

	private final JavaBinModule ooxmlNiceXmlMessages = binModule(
			"com.github.oowekyala.ooxml", "nice-xml-messages", "3.1");

	private final JavaBinModule pcollections = binModule("org.pcollections",
			"pcollections", "4.0.2");

	private static final String PMD_VER = "7.4.0";
	private final JavaModule pmdAnt = binModule("net.sourceforge.pmd",
			"pmd-ant", PMD_VER);
	private final JavaModule pmdCore = binModule("net.sourceforge.pmd",
			"pmd-core", PMD_VER);
	private final JavaModule pmdJava = binModule("net.sourceforge.pmd",
			"pmd-java", PMD_VER);
	private final JavaModule pmdLangTest = binModule("net.sourceforge.pmd",
			"pmd-lang-test", PMD_VER);
	private final JavaModule pmdTest = binModule("net.sourceforge.pmd",
			"pmd-test", PMD_VER);
	private final JavaModule pmdTestSchema = binModule("net.sourceforge.pmd",
			"pmd-test-schema", PMD_VER);
	private final List<JavaModule> pmdModules = List.of(pmdAnt, pmdCore,
			pmdJava, pmdLangTest, pmdTest, pmdTestSchema);

	private final JavaBinModule jcommander = binModule("com.beust",
			"jcommander", "1.82");

	private final JavaBinModule saxonHe = binModule("net.sf.saxon", "Saxon-HE",
			"12.5");

	private static final String SLF4J_VER = "2.0.16";
	private final JavaBinModule slf4jApi = binModule("org.slf4j", "slf4j-api",
			SLF4J_VER);
	private final JavaBinModule slf4jJulToSlf4j = binModule("org.slf4j",
			"jul-to-slf4j", SLF4J_VER);
	private final JavaBinModule slf4jSimple = binModule("org.slf4j",
			"slf4j-simple", SLF4J_VER);

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

	private final JavaBinModule xmlresolver = binModule("org.xmlresolver",
			"xmlresolver", "6.0.8");

	private JavaSrcModule iwantEntry() {
		return lazy(() -> module("iwant-entry")
				.locationUnderWsRoot("essential/iwant-entry").noMainJava()
				.mainJava("as-some-developer/with/java").noTestJava().mainDeps()
				.end());
	}

	private JavaSrcModule iwantApiModel() {
		return lazy(
				() -> essentialModule("api-model").mainDeps().testDeps().end());
	}

	private JavaBinModule iwantWsRootMarker() {
		return lazy(() -> JavaBinModule
				.providing(Source.underWsroot("essential/iwant-wsroot-marker"))
				.end());
	}

	private JavaSrcModule iwantIwantWsrootFinder() {
		return lazy(() -> essentialModule("iwant-wsroot-finder")
				.mainDeps(iwantWsRootMarker()).testDeps(commonsIo).end());
	}

	private JavaSrcModule iwantTestarea() {
		return lazy(() -> privateModule("testarea").noTestJava()
				.mainDeps(iwantEntry()).mainDeps(junitJupiterModules()).end());
	}

	private JavaSrcModule iwantEntrymocks() {
		return lazy(() -> privateModule("entrymocks")
				.mainDeps(iwantEntry(), iwantTestarea()).testDeps().end());
	}

	private JavaSrcModule iwantEntry2() {
		return lazy(
				() -> essentialModule("entry2")
						.mainDeps(iwantEntry()).testDeps(iwantEntrymocks(),
								iwantIwantWsrootFinder(), iwantTestarea())
						.end());
	}

	private JavaSrcModule iwantCoreservices() {
		return lazy(() -> essentialModule("coreservices")
				.mainDeps(iwantApiModel(), iwantEntry(), iwantEntry2())
				.testDeps(iwantTestarea()).end());
	}

	private JavaSrcModule iwantApimocks() {
		return lazy(() -> privateModule("apimocks")
				.mainDeps(iwantApiModel(), iwantCoreservices(), iwantEntry(),
						iwantEntrymocks(), iwantTestarea())
				.mainDeps(ctx.iwantPlugin().junit5runner().withDependencies())
				.noTestJava().end());
	}

	private JavaSrcModule iwantApiTarget() {
		return lazy(() -> essentialModule("api-target")
				.mainDeps(iwantApiModel()).testDeps().end());
	}

	private JavaSrcModule iwantApiCore() {
		return lazy(() -> essentialModule("api-core")
				.mainDeps(iwantApiModel(), iwantApiTarget(),
						iwantCoreservices(), iwantEntry())
				.testDeps(iwantApimocks()).end());
	}

	private JavaSrcModule iwantApiAntrunner() {
		return lazy(() -> essentialModule("api-antrunner").noMainResources()
				.noTestResources().mainDeps(ant, iwantEntry()).testDeps()
				.end());
	}

	private JavaSrcModule iwantCoreDownload() {
		return lazy(() -> essentialModule("core-download")
				.mainDeps(iwantApiCore(), iwantApiModel(), iwantApiTarget(),
						iwantCoreservices(), iwantEntry(), iwantEntry2())
				.testDeps(iwantApimocks(), iwantTestarea()).end());
	}

	private JavaSrcModule iwantApiZip() {
		return lazy(() -> essentialModule("api-zip")
				.testResources("src/test/resources").mainDeps(ant, antLauncher,
						iwantApiCore(), iwantApiModel(), iwantApiTarget())
				.testDeps(iwantApimocks()).end());
	}

	private JavaSrcModule iwantApiJavamodules() {
		return lazy(() -> essentialModule("api-javamodules")
				.mainDeps(iwantApiAntrunner(), iwantApiCore(), iwantApiModel(),
						iwantApiTarget(), iwantApiZip(), iwantCoreDownload(),
						iwantCoreservices(), iwantEntry())
				.testDeps(iwantApimocks(), iwantTestarea(), guava, guavaTestlib)
				.end());
	}

	private JavaSrcModule iwantApiWsdef() {
		return lazy(() -> essentialModule("api-wsdef").noTestJava()
				.mainDeps(iwantApiModel(), iwantApiJavamodules()).end());
	}

	private JavaSrcModule iwantCoreJavamodules() {
		return lazy(() -> essentialModule("core-javamodules")
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantApiTarget(), iwantApiZip(), iwantCoreDownload())
				.testDeps().end());
	}

	private JavaSrcModule iwantApiBash() {
		return lazy(() -> essentialModule("api-bash")
				.mainResources("src/main/resources")
				.testResources("src/test/resources")
				.mainDeps(iwantApiCore(), iwantApiJavamodules(),
						iwantApiModel(), iwantApiTarget(), iwantApiWsdef(),
						iwantCoreservices(), iwantEntry())
				.testDeps(iwantApimocks()).end());
	}

	private JavaSrcModule iwantPlannerApi() {
		return lazy(() -> essentialModule("planner-api").mainDeps(iwantEntry())
				.testDeps().end());
	}

	private JavaSrcModule iwantPlannerMocks() {
		return lazy(() -> essentialModule("planner-mocks").noTestJava()
				.mainDeps(iwantPlannerApi()).mainDeps(junitJupiterModules())
				.end());
	}

	private JavaSrcModule iwantPlanner() {
		return lazy(() -> essentialModule("planner")
				.mainDeps(iwantEntry(), iwantPlannerApi())
				.testDeps(iwantPlannerMocks()).end());
	}

	private JavaSrcModule iwantEclipseSettings() {
		return lazy(() -> essentialModule("eclipse-settings")
				.mainDeps(iwantApiCore(), iwantApiJavamodules(),
						iwantApiModel(), iwantApiTarget(), iwantEntry())
				.testDeps(iwantApimocks()).end());
	}

	private JavaSrcModule iwantCoreJavafinder() {
		return lazy(
				() -> essentialModule("core-javafinder")
						.mainDeps(iwantApiJavamodules(), iwantApiModel(),
								iwantApiWsdef(), iwantEntry())
						.testDeps().end());
	}

	private JavaSrcModule iwantEntry3() {
		return lazy(() -> essentialModule("entry3")
				.mainDeps(iwantApiBash(), iwantApiCore(), iwantApiJavamodules(),
						iwantApiModel(), iwantApiTarget(), iwantApiWsdef(),
						iwantCoreDownload(), iwantCoreJavafinder(),
						iwantCoreservices(), iwantEntry(), iwantEntry2(),
						iwantIwantWsrootFinder(), iwantPlanner(),
						iwantPlannerApi())
				.testDeps(iwantApimocks(), iwantEclipseSettings(),
						iwantEntrymocks(), iwantPlannerMocks(), iwantTestarea())
				.end());
	}

	private JavaSrcModule iwantEmbedded() {
		return lazy(() -> essentialModule("embedded")
				.mainDeps(iwantApiModel(), iwantApiJavamodules(),
						iwantApiTarget(), iwantApiWsdef(), iwantCoreservices(),
						iwantEntry(), iwantEntry3())
				.testDeps(iwantApimocks(), iwantApiCore(), iwantTestarea())
				.end());
	}

	private JavaSrcModule iwantCoreAnt() {
		return lazy(() -> essentialModule("core-ant")
				.mainDeps(iwantApiAntrunner(), iwantApiCore(), iwantApiModel(),
						iwantApiTarget(), iwantCoreservices(), iwantEntry())
				.testDeps(iwantApimocks(), iwantCoreDownload(), iwantEmbedded(),
						iwantTestarea())
				.end());
	}

	private JavaSrcModule iwantDocs() {
		return lazy(
				() -> privateModule("docs").noMainJava().noTestJava().end());
	}

	private JavaSrcModule iwantExampleWsdef() {
		return lazy(() -> essentialModule("example-wsdef").noTestJava()
				.mainDeps(iwantApiCore(), iwantApiJavamodules(),
						iwantApiModel(), iwantApiTarget(), iwantApiWsdef(),
						iwantEntry3(), iwantEclipseSettings())
				.end());
	}

	final JavaSrcModule iwantMockWsroot() {
		return lazy(() -> withMockrootMainJavas(privateModule("mock-wsroot"))
				.noTestJava().mainDeps().end());
	}

	private static IwantSrcModuleSpex withMockrootMainJavas(
			IwantSrcModuleSpex mod) {
		mod.noMainJava();
		mod.mainJava("essential/iwant-api-antrunner/src/main/java");
		mod.mainJava("essential/iwant-api-javamodules/src/main/java");
		mod.mainJava("essential/iwant-apimocks/src/main/java");
		mod.mainJava("essential/iwant-api-model/src/main/java");
		mod.mainJava("essential/iwant-api-wsdef/src/main/java");
		mod.mainJava("essential/iwant-coreservices/src/main/java");
		mod.mainJava("essential/iwant-embedded/src/main/java");
		mod.mainJava("essential/iwant-entry/as-some-developer/with/java");
		mod.mainJava("essential/iwant-entry2/src/main/java");
		mod.mainJava("essential/iwant-entry3/src/main/java");
		mod.mainJava("essential/iwant-iwant-wsroot-finder/src/main/java");
		return mod;
	}

	private JavaSrcModule iwantTestresources() {
		return lazy(() -> privateModule("testresources").noTestJava()
				.mainResources("src/main/resources").end());
	}

	private JavaSrcModule iwantPluginAnt() {
		return lazy(() -> optionalModule("plugin-ant")
				.mainDeps(ant, antLauncher, iwantApiCore(), iwantApiModel())
				.testDeps(iwantApimocks(), iwantEntry(), iwantTestarea(),
						iwantTestresources())
				.end());
	}

	private JavaSrcModule iwantPluginFindbugs() {
		return lazy(() -> optionalModule("plugin-findbugs")
				.testResources("src/test/resources")
				.mainDeps(commonsIo, iwantApiAntrunner(), iwantApiCore(),
						iwantApiJavamodules(), iwantApiModel(),
						iwantApiTarget(), iwantCoreAnt(), iwantCoreDownload(),
						iwantEntry(), iwantEntry3(), iwantPluginAnt())
				.testDeps(iwantApimocks(), iwantEmbedded(), iwantTestarea())
				.end());
	}

	private JavaSrcModule iwantPluginGithub() {
		return lazy(
				() -> optionalModule("plugin-github").mainDeps(iwantApiCore(),
						iwantApiModel(), iwantApiTarget(), iwantApiZip(),
						iwantCoreDownload(), iwantEntry3(), iwantPluginAnt())
						.testDeps().end());
	}

	private JavaSrcModule iwantPluginJacoco() {
		return lazy(() -> optionalModule("plugin-jacoco")
				.mainDeps(commonsIo, iwantApiAntrunner(), iwantApiCore(),
						iwantPluginJunit5Runner(), iwantApiModel(),
						iwantApiJavamodules(), iwantApiTarget(), iwantApiZip(),
						iwantCoreAnt(), iwantCoreDownload(), iwantEntry(),
						iwantEntry3(), iwantPluginAnt())
				.testDeps(iwantApimocks(), iwantEmbedded(), iwantTestarea())
				.end());
	}

	private JavaSrcModule iwantPluginJunit5Runner() {
		return lazy(() -> module("iwant-plugin-junit5runner")
				.locationUnderWsRoot("optional/iwant-plugin-junit5runner")
				.noMainResources().noTestJava().noTestResources()
				.mainDeps(iwantEntry()).mainDeps(junitJupiterModules())
				.testDeps().end());
	}

	// TODO don't depend directly on asm, jaxen, ...: pmd depends on them
	private JavaSrcModule iwantPluginPmd() {
		return lazy(() -> optionalModule("plugin-pmd")
				.testResources("src/test/resources").mainDeps(pmdModules)
				.mainDeps(ant, asm, commonsIo, commonsLang3, iwantApiCore(),
						iwantApiModel(), iwantApiTarget(), iwantEntry(),
						ooxmlNiceXmlMessages, pcollections, jaxen, saxonHe,
						slf4jApi, slf4jJulToSlf4j, slf4jSimple, xmlresolver)
				.testDeps(iwantApimocks(), iwantTestarea(),
						iwantTestresources())
				.end());
	}

	private JavaSrcModule iwantPluginTestng() {
		return lazy(() -> optionalModule("plugin-testng")
				.mainDeps(iwantApiJavamodules(), testng).testDeps(iwantEntry())
				.end());
	}

	private JavaSrcModule iwantPluginWar() {
		return lazy(() -> optionalModule("plugin-war")
				.mainDeps(ant, antLauncher, iwantApiModel())
				.testDeps(iwantApimocks(), iwantApiZip(), iwantEntry(),
						iwantPluginAnt(), iwantTestarea(), iwantTestresources())
				.end());
	}

	private JavaSrcModule iwantTutorialWsdefs() {
		return lazy(() -> privateModule("tutorial-wsdefs")
				.scalaVersion(SCALA_VER).noMainJava().noTestJava()
				.mainJava("src")
				.mainDeps(commonsMath, iwantApiBash(), iwantApiCore(),
						iwantApiJavamodules(), iwantApiModel(),
						iwantApiTarget(), iwantApiWsdef(), iwantApiZip(),
						iwantCoreAnt(), iwantCoreDownload(),
						iwantCoreJavamodules(), iwantCoreservices(),
						iwantCoreJavafinder(), iwantEntry3(),
						iwantEclipseSettings(), iwantPluginAnt(),
						iwantPluginFindbugs(), iwantPluginGithub(),
						iwantPluginJacoco(), iwantPluginPmd(),
						iwantPluginTestng(), iwantPluginWar(), scalaLibrary,
						testng)
				.mainDeps(junitJupiterModules()).end());
	}

	private final Target extendedIwantEnumsJava = new ExtendedIwantEnums(
			"extended-iwant-enums");

	private final Target extendedIwantEnumsClasses = JavaClasses.with()
			.name(extendedIwantEnumsJava.name() + "-classes")
			.srcDirs(extendedIwantEnumsJava).end();

	private final JavaModule iwantExtendedEnums() {
		return lazy(() -> JavaBinModule
				.providing(extendedIwantEnumsClasses, extendedIwantEnumsJava)
				.end());
	}

	private JavaSrcModule iwantEntryTests() {
		return lazy(() -> privateModule("entry-tests").noMainJava()
				.testResources("src/test/resources")
				.testDeps(commonsIo, guava, guavaTestlib, iwantApiCore(),
						iwantApiModel(), iwantApiTarget(), iwantEntry(),
						iwantEntrymocks(), iwantIwantWsrootFinder(),
						iwantTestarea(), nettyBuffer, nettyCodec, nettyCodecDns,
						nettyCodecHttp, nettyCodecHttp2, nettyCommon,
						nettyHandler, nettyHandlerProxy, nettyResolver,
						nettyResolverDns, nettyTransport, vertxCore, vertxWeb)
				.end());
	}

	private final JavaSrcModule iwantTests() {
		SortedSet<JavaModule> deps = new TreeSet<>();
		deps.addAll(runtimeDepsOf(iwantEntry(), iwantEntry2(), iwantEntry3(),
				iwantEmbedded()));
		deps.addAll(
				runtimeDepsOf(iwantCoreJavamodules(), iwantEclipseSettings()));
		deps.addAll(runtimeDepsOf(iwantApimocks()));
		deps.addAll(runtimeDepsOf(iwantPluginFindbugs(), iwantPluginGithub(),
				iwantPluginJacoco()));

		return lazy(() -> privateModule("tests").noMainJava()
				.testResources("src/test/resources")
				.testDeps(iwantExtendedEnums()).testDeps(deps)
				.testDeps(commonsIo).end());
	}

	private List<JavaSrcModule> modulesNotDependedByOthers() {
		List<JavaSrcModule> m = new ArrayList<>();

		// internal:
		m.add(iwantDocs());
		m.add(iwantEntryTests());
		m.add(iwantTests());
		return m;
	}

}
