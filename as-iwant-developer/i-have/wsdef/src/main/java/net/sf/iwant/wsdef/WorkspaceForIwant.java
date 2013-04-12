package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.EmmaCoverage;
import net.sf.iwant.api.EmmaInstrumentation;
import net.sf.iwant.api.EmmaReport;
import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;

public class WorkspaceForIwant implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(emmaCoverageReport());
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
		return new TreeSet<JavaModule>(Arrays.asList(commonsMath(),
				iwantApiJavamodules(), iwantApiModel(), iwantDistillery(),
				iwantDistillery2(), iwantDocs(), iwantExampleWsdef(),
				iwantMockWsroot(), iwantTestarea(), iwantTestrunner(),
				iwantTutorialWsdefs(), junit()));
	}

	// the targets

	private static Path emma() {
		return TestedIwantDependencies.emma();
	}

	private static EmmaCoverage emmaCoverage(JavaSrcModule module,
			String testSuiteName) {
		return EmmaCoverage
				.with()
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.emma(emma())
				.module(module)
				.mainClassAndArguments("org.junit.runner.JUnitCore",
						testSuiteName).end();
	}

	private static Target emmaCoverageReport() {
		return EmmaReport
				.with()
				.name("emma-coverage-report")
				.emma(emma())
				.instrumentations(apiJavamodulesEmmaInstrumentation(),
						apiModelEmmaInstrumentation(),
						distilleryEmmaInstrumentation(),
						distillery2EmmaInstrumentation(),
						testareaEmmaInstrumentation(),
						testrunnerEmmaInstrumentation())
				.coverages(apiJavamodulesEmmaCoverage(),
						apiModelEmmaCoverage(), distilleryEmmaCoverage(),
						distillery2EmmaCoverage(), testrunnerEmmaCoverage())
				.end();
	}

	private static EmmaCoverage apiJavamodulesEmmaCoverage() {
		return emmaCoverage(iwantApiJavamodules(),
				"net.sf.iwant.api.javamodules.IwantApiJavamodulesSuite");
	}

	private static EmmaInstrumentation apiJavamodulesEmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantApiJavamodules()).using(emma());
	}

	private static EmmaCoverage apiModelEmmaCoverage() {
		return emmaCoverage(iwantApiModel(),
				"net.sf.iwant.api.model.IwantApiModelSuite");
	}

	private static EmmaInstrumentation apiModelEmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantApiModel()).using(emma());
	}

	private static EmmaCoverage distilleryEmmaCoverage() {
		return emmaCoverage(iwantDistillery(),
				"net.sf.iwant.IwantDistillerySuite");
	}

	private static EmmaInstrumentation distilleryEmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantDistillery()).using(emma());
	}

	private static EmmaCoverage distillery2EmmaCoverage() {
		return emmaCoverage(iwantDistillery2(),
				"net.sf.iwant.IwantDistillery2Suite");
	}

	private static EmmaInstrumentation distillery2EmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantDistillery2()).using(emma());
	}

	private static EmmaCoverage testrunnerEmmaCoverage() {
		return emmaCoverage(iwantTestrunner(),
				"net.sf.iwant.testrunner.IwantTestRunnerTest");
	}

	private static EmmaInstrumentation testareaEmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantTestarea()).using(emma());
	}

	private static EmmaInstrumentation testrunnerEmmaInstrumentation() {
		return EmmaInstrumentation.of(iwantTestrunner()).using(emma());
	}

	// the modules

	private static JavaModule commonsMath() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("commons-math").name("commons-math").version("1.2"));
	}

	private static JavaSrcModule iwantApiJavamodules() {
		return iwantSrcModule("api-javamodules").mainDeps(iwantApiModel())
				.testDeps(junit()).end();
	}

	private static JavaSrcModule iwantApiModel() {
		return iwantSrcModule("api-model").mainDeps().testDeps(junit()).end();
	}

	private static JavaSrcModule iwantDistillery() {
		return iwantSrcModule("distillery")
				.mainJava("as-some-developer/with/java")
				.testResources("src/test/resources")
				.mainDeps(iwantTestarea(), junit())
				.testDeps(iwantDistilleryClasspathMarker()).end();
	}

	private static JavaBinModule iwantDistilleryClasspathMarker() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-distillery/classpath-marker"));
	}

	private static JavaSrcModule iwantDistillery2() {
		return iwantSrcModule("distillery2")
				.mainDeps(iwantApiJavamodules(), iwantApiModel(),
						iwantDistillery())
				.testDeps(iwantDistilleryClasspathMarker(), iwantTestarea(),
						junit()).end();
	}

	private static JavaModule iwantDocs() {
		return iwantSrcModule("docs").end();
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
		mod.mainJava("iwant-api-javamodules/src/test/java");
		mod.mainJava("iwant-api-javamodules/src/main/java");
		mod.mainJava("iwant-api-model/src/test/java");
		mod.mainJava("iwant-api-model/src/main/java");
		mod.mainJava("iwant-distillery/src/test/java");
		mod.mainJava("iwant-distillery/as-some-developer/with/java");
		mod.mainJava("iwant-distillery/src/main/java");
		mod.mainJava("iwant-distillery2/src/test/java");
		mod.mainJava("iwant-distillery2/src/main/java");
		mod.mainJava("iwant-testarea/src/main/java");
		mod.mainJava("iwant-testrunner/src/main/java");
		return mod.mainDeps(junit()).end();
	}

	private static JavaSrcModule iwantTestarea() {
		return iwantSrcModule("testarea").noTestJava()
				.mainDeps(iwantTestareaClassdir()).end();
	}

	private static JavaBinModule iwantTestareaClassdir() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-testarea/testarea-classdir"));
	}

	private static JavaSrcModule iwantTestrunner() {
		return iwantSrcModule("testrunner").mainDeps(junit()).end();
	}

	private static JavaModule iwantTutorialWsdefs() {
		return iwantSrcModule("tutorial-wsdefs")
				.noMainJava()
				.noTestJava()
				.mainJava("src")
				.mainDeps(commonsMath(), iwantApiJavamodules(),
						iwantApiModel(), iwantDistillery2()).end();
	}

	private static JavaModule junit() {
		return JavaBinModule.providing(FromRepository.ibiblio().group("junit")
				.name("junit").version("4.8.2"));
	}

}
