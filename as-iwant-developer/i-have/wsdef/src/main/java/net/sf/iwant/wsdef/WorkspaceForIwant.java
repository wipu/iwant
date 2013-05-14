package net.sf.iwant.wsdef;

import java.util.Arrays;
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
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;

public class WorkspaceForIwant implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(emmaCoverageReport(), listOfExternalDeps(),
				localTutorials());
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

	private static SortedSet<JavaModule> allModules() {
		return new TreeSet<JavaModule>(Arrays.asList(ant(), bcel(),
				commonsMath(), findbugs(), iwantApiJavamodules(),
				iwantApimocks(), iwantApiModel(), iwantCoreservices(),
				iwantDistillery(), iwantDistillery2(), iwantDocs(),
				iwantExampleWsdef(), iwantMockWsroot(), iwantPluginAnt(),
				iwantPluginFindbugs(), iwantTestarea(), iwantTestresources(),
				iwantTutorialWsdefs(), junit()));
	}

	// the targets

	// tutorials

	private static Target localTutorials() {
		ConcatenatedBuilder list = Concatenated.named("local-tutorials");
		list.pathTo(bootstrappingLocallyHtml());
		list.pathTo(creatingWsdef());
		return list.end();
	}

	private static Target bootstrappingLocallyHtml() {
		return new Descripted("bootstrapping-locally", tutorialWsdefSrc(),
				Source.underWsroot(""), null);
	}

	private static Source tutorialWsdefSrc() {
		return Source.underWsroot("iwant-tutorial-wsdefs/src");
	}

	private static Target creatingWsdef() {
		return new Descripted("creating-wsdef", tutorialWsdefSrc(), null,
				bootstrappingLocallyHtml());
	}

	// others

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
		return emmaTargets.emmaReport();

	}

	private static Path emmaFilter() {
		ConcatenatedBuilder filter = Concatenated.named("emma-filter");
		// only used in the tutorial, not "real" code:
		filter.string("-com.example.*\n");
		return filter.end();
	}

	private static Target listOfExternalDeps() {
		ConcatenatedBuilder deps = Concatenated.named("list-of-ext-deps");
		deps.pathTo(ant().mainArtifact()).string("\n");
		deps.pathTo(bcel().mainArtifact()).string("\n");
		deps.pathTo(commonsMath().mainArtifact()).string("\n");
		deps.pathTo(findbugs().mainArtifact()).string("\n");
		return deps.end();
	}

	// the modules

	/**
	 * TODO reuse with TestedIwantDependencies
	 * 
	 * @return
	 */
	private static JavaModule ant() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("org/apache/ant").name("ant").version("1.7.1"));
	}

	/**
	 * TODO declare that findbugs depends on this
	 */
	private static JavaModule bcel() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("org/apache/bcel").name("bcel").version("5.2"));
	}

	private static JavaModule commonsMath() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("commons-math").name("commons-math").version("1.2"));
	}

	private static JavaModule findbugs() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("findbugs").name("findbugs").version("1.0.0"));
	}

	private static JavaSrcModule iwantApiJavamodules() {
		return iwantSrcModule("api-javamodules")
				.mainDeps(iwantApiModel())
				.testDeps(junit())
				.testSuiteName(
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
				.testSuiteName("net.sf.iwant.api.model.IwantApiModelSuite")
				.end();
	}

	private static JavaSrcModule iwantCoreservices() {
		return iwantSrcModule("coreservices")
				.mainDeps(iwantApiModel(), iwantDistillery())
				.testDeps(iwantTestarea(), junit())
				.testSuiteName(
						"net.sf.iwant.coreservices." + "IwantCoreservicesSuite")
				.end();
	}

	private static JavaSrcModule iwantDistillery() {
		return iwantSrcModule("distillery")
				.mainJava("as-some-developer/with/java")
				.testResources("src/test/resources")
				.mainDeps(iwantTestarea(), junit())
				.testDeps(iwantDistilleryClasspathMarker())
				.testSuiteName("net.sf.iwant.IwantDistillerySuite").end();
	}

	private static JavaBinModule iwantDistilleryClasspathMarker() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-distillery/classpath-marker"));
	}

	private static JavaSrcModule iwantDistillery2() {
		return iwantSrcModule("distillery2")
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantCoreservices(), iwantDistillery())
				.testDeps(iwantApimocks(), iwantDistilleryClasspathMarker(),
						iwantTestarea(), junit())
				.testSuiteName("net.sf.iwant.IwantDistillery2Suite").end();
	}

	private static JavaModule iwantDocs() {
		return iwantSrcModule("docs").noMainJava().noTestJava().end();
	}

	private static JavaModule iwantExampleWsdef() {
		return iwantSrcModule("example-wsdef")
				.noTestJava()
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantDistillery2()).end();
	}

	private static JavaModule iwantMockWsroot() {
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

	private static JavaModule iwantPluginAnt() {
		return iwantSrcModule("plugin-ant")
				.mainDeps(ant(), iwantApiModel())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantTestarea(), iwantTestresources())
				.testSuiteName("net.sf.iwant.plugin.ant.IwantPluginAntSuite")
				.end();
	}

	private static JavaModule iwantPluginFindbugs() {
		return iwantSrcModule("plugin-findbugs")
				.mainDeps(bcel(), iwantApiJavamodules(), iwantApiModel(),
						findbugs())
				.testDeps(junit(), iwantApimocks(), iwantDistillery(),
						iwantTestarea())
				.testSuiteName(
						"net.sf.iwant.plugin.findbugs."
								+ "IwantPluginFindbugsSuite").end();
	}

	private static JavaSrcModule iwantTestarea() {
		return iwantSrcModule("testarea").noTestJava()
				.mainDeps(iwantTestareaClassdir()).end();
	}

	private static JavaBinModule iwantTestareaClassdir() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-testarea/testarea-classdir"));
	}

	private static JavaModule iwantTestresources() {
		return iwantSrcModule("testresources").noTestJava()
				.mainResources("src/main/resources").end();
	}

	private static JavaModule iwantTutorialWsdefs() {
		return iwantSrcModule("tutorial-wsdefs")
				.noMainJava()
				.noTestJava()
				.mainJava("src")
				.mainDeps(commonsMath(), iwantApiJavamodules(),
						iwantApiModel(), iwantDistillery2(), iwantPluginAnt())
				.end();
	}

	private static JavaModule junit() {
		return JavaBinModule.providing(FromRepository.ibiblio().group("junit")
				.name("junit").version("4.8.2"));
	}

}
