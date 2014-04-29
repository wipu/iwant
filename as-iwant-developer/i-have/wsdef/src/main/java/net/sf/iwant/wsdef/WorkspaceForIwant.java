package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.EmmaTargetsOfJavaModules;
import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.plugin.findbugs.FindbugsDistribution;
import net.sf.iwant.plugin.findbugs.FindbugsOutputFormat;
import net.sf.iwant.plugin.findbugs.FindbugsReport;
import net.sf.iwant.plugin.findbugs.FindbugsReport.FindbugsReportSpex;

public class WorkspaceForIwant implements IwantWorkspace {

	private final FindbugsDistribution findbugs = FindbugsDistribution
			.ofVersion("2.0.3");

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(emmaCoverageReport(), findbugsReport(),
				listOfExternalDeps(), localWebsite(), remoteWebsite());
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(allModules()).end());
	}

	private static IwantSrcModuleSpex iwantSrcModule(String subName) {
		String fullName = "iwant-" + subName;
		return JavaSrcModule.with().name(fullName)
				.locationUnderWsRoot(fullName).mainJava("src/main/java")
				.testJava("src/test/java");
	}

	private static SortedSet<JavaSrcModule> allSrcModules() {
		return new TreeSet<JavaSrcModule>(Arrays.asList(iwantApiJavamodules(),
				iwantApimocks(), iwantApiModel(), iwantCoreservices(),
				iwantDistillery(), iwantDistillery2(), iwantDocs(),
				iwantExampleWsdef(), iwantMockWsroot(), iwantPluginAnt(),
				iwantPluginFindbugs(), iwantPluginPmd(), iwantPluginWar(),
				iwantTestarea(), iwantTestresources(), iwantTutorialWsdefs()));
	}

	private static SortedSet<JavaModule> allModules() {
		SortedSet<JavaModule> all = new TreeSet<JavaModule>();
		all.addAll(allSrcModules());
		all.addAll(Arrays.asList(ant(), commonsMath(), junit()));
		return all;
	}

	// the targets

	private static Path emma() {
		return TestedIwantDependencies.emma();
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
		FindbugsReportSpex report = FindbugsReport
				.with()
				.name(name)
				.outputFormat(outputFormat)
				.using(findbugs, TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar());
		for (JavaSrcModule mod : modules) {
			if (mod.mainArtifact() != null) {
				JavaClassesAndSources main = new JavaClassesAndSources(
						mod.mainArtifact(), mod.mainJavasAsPaths());
				report.classesToAnalyze(main);
			}
			if (mod.testArtifact() != null) {
				JavaClassesAndSources test = new JavaClassesAndSources(
						mod.testArtifact(), mod.testJavasAsPaths());
				report.classesToAnalyze(test);
			}
			for (JavaModule aux : mod.effectivePathForTestRuntime()) {
				if (aux instanceof JavaBinModule) {
					report.auxClasses(aux.mainArtifact());
				}
			}
		}
		return report.end();
	}

	private static Target listOfExternalDeps() {
		ConcatenatedBuilder deps = Concatenated.named("list-of-ext-deps");
		deps.pathTo(ant().mainArtifact()).string("\n");
		deps.pathTo(antLauncher().mainArtifact()).string("\n");
		deps.pathTo(asm().mainArtifact()).string("\n");
		deps.pathTo(commonsIo().mainArtifact()).string("\n");
		deps.pathTo(commonsMath().mainArtifact()).string("\n");
		deps.pathTo(jaxen().mainArtifact()).string("\n");
		deps.pathTo(junit().mainArtifact()).string("\n");
		deps.pathTo(pmd().mainArtifact()).string("\n");
		return deps.end();
	}

	private static Target localTutorial() {
		return Tutorial.local();
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
	private static JavaModule ant() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("org/apache/ant").name("ant")
						.version("1.7.1")).end();
	}

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private static JavaModule antLauncher() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("org/apache/ant")
						.name("ant-launcher").version("1.7.1")).end();
	}

	private static JavaModule asm() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("asm").name("asm")
						.version("3.2")).end();
	}

	private static JavaModule commonsIo() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("org/apache/commons")
						.name("commons-io").version("1.3.2")).end();
	}

	private static JavaModule commonsMath() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("commons-math")
						.name("commons-math").version("1.2")).end();
	}

	private static JavaSrcModule iwantApiJavamodules() {
		return iwantSrcModule("api-javamodules")
				.mainDeps(iwantApiModel())
				.testDeps(junit())
				.testedBy(
						"net.sf.iwant.api.javamodules.IwantApiJavamodulesSuite")
				.end();
	}

	private static JavaSrcModule iwantApimocks() {
		return iwantSrcModule("apimocks")
				.mainDeps(iwantApiModel(), iwantCoreservices(),
						iwantDistillery()).noTestJava().end();
	}

	private static JavaSrcModule iwantApiModel() {
		return iwantSrcModule("api-model").mainDeps().testDeps(junit())
				.testedBy("net.sf.iwant.api.model.IwantApiModelSuite").end();
	}

	private static JavaSrcModule iwantCoreservices() {
		return iwantSrcModule("coreservices")
				.mainDeps(iwantApiModel(), iwantDistillery())
				.testDeps(iwantTestarea(), junit())
				.testedBy(
						"net.sf.iwant.coreservices." + "IwantCoreservicesSuite")
				.end();
	}

	private static JavaSrcModule iwantDistillery() {
		return iwantSrcModule("distillery")
				.mainJava("as-some-developer/with/java")
				.testResources("src/test/resources")
				.mainDeps(iwantTestarea(), junit())
				.testDeps(iwantDistilleryClasspathMarker())
				.testedBy("net.sf.iwant.IwantDistillerySuite").end();
	}

	private static JavaBinModule iwantDistilleryClasspathMarker() {
		return JavaBinModule.providing(
				Source.underWsroot("iwant-distillery/classpath-marker")).end();
	}

	private static JavaSrcModule iwantDistillery2() {
		return iwantSrcModule("distillery2")
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantCoreservices(), iwantDistillery())
				.testDeps(iwantApimocks(), iwantDistilleryClasspathMarker(),
						iwantTestarea(), junit())
				.testedBy("net.sf.iwant.IwantDistillery2Suite").end();
	}

	private static JavaSrcModule iwantDocs() {
		return iwantSrcModule("docs").noMainJava().noTestJava().end();
	}

	private static JavaSrcModule iwantExampleWsdef() {
		return iwantSrcModule("example-wsdef")
				.noTestJava()
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantDistillery2()).end();
	}

	private static JavaSrcModule iwantMockWsroot() {
		IwantSrcModuleSpex mod = iwantSrcModule("mock-wsroot").noMainJava()
				.noTestJava();
		mod.mainJava("iwant-api-javamodules/src/main/java");
		mod.mainJava("iwant-apimocks/src/main/java");
		mod.mainJava("iwant-api-model/src/main/java");
		mod.mainJava("iwant-coreservices/src/main/java");
		mod.mainJava("iwant-distillery/as-some-developer/with/java");
		mod.mainJava("iwant-distillery/src/main/java");
		mod.mainJava("iwant-distillery2/src/main/java");
		mod.mainJava("iwant-testarea/src/main/java");
		return mod.mainDeps(junit()).end();
	}

	private static JavaSrcModule iwantPluginAnt() {
		return iwantSrcModule("plugin-ant")
				.testResources("src/test/resources")
				.mainDeps(ant(), antLauncher(), iwantApiModel())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantTestarea(), iwantTestresources())
				.testedBy("net.sf.iwant.plugin.ant.IwantPluginAntSuite").end();
	}

	private static JavaSrcModule iwantPluginFindbugs() {
		return iwantSrcModule("plugin-findbugs")
				.testResources("src/test/resources")
				.mainDeps(commonsIo(), iwantApiJavamodules(), iwantApiModel(),
						iwantDistillery2(), iwantPluginAnt())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantTestarea())
				.testedBy(
						"net.sf.iwant.plugin.findbugs."
								+ "IwantPluginFindbugsSuite").end();
	}

	private static JavaSrcModule iwantPluginPmd() {
		// TODO don't depend directly on asm, jaxen: pmd depends on them
		return iwantSrcModule("plugin-pmd")
				.testResources("src/test/resources")
				.mainDeps(ant(), asm(), commonsIo(), iwantApiModel(), jaxen(),
						pmd())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantTestarea(), iwantTestresources())
				.testedBy("net.sf.iwant.plugin.pmd.IwantPluginPmdSuite").end();
	}

	private static JavaSrcModule iwantPluginWar() {
		return iwantSrcModule("plugin-war")
				.mainDeps(ant(), antLauncher(), iwantApiModel())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantPluginAnt(), iwantTestarea(), iwantTestresources())
				.testedBy("net.sf.iwant.plugin.war.IwantPluginWarSuite").end();
	}

	private static JavaSrcModule iwantTestarea() {
		return iwantSrcModule("testarea").noTestJava()
				.mainDeps(iwantTestareaClassdir()).end();
	}

	private static JavaBinModule iwantTestareaClassdir() {
		return JavaBinModule.providing(
				Source.underWsroot("iwant-testarea/testarea-classdir")).end();
	}

	private static JavaSrcModule iwantTestresources() {
		return iwantSrcModule("testresources").noTestJava()
				.mainResources("src/main/resources").end();
	}

	private static JavaSrcModule iwantTutorialWsdefs() {
		return iwantSrcModule("tutorial-wsdefs")
				.noMainJava()
				.noTestJava()
				.mainJava("src")
				.mainDeps(commonsMath(), iwantApiJavamodules(),
						iwantApiModel(), iwantDistillery2(), iwantPluginAnt(),
						iwantPluginFindbugs(), iwantPluginPmd(),
						iwantPluginWar()).end();
	}

	private static JavaModule jaxen() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("jaxen").name("jaxen")
						.version("1.1.4")).end();
	}

	private static JavaModule junit() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("junit").name("junit")
						.version("4.8.2")).end();
	}

	private static JavaModule pmd() {
		// TODO document dependency to asm, jaxen
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("pmd").name("pmd")
						.version("4.3")).end();
	}

}
