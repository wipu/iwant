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
import net.sf.iwant.api.JavaBinModule;
import net.sf.iwant.api.JavaClassesAndSources;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;
import net.sf.iwant.api.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TestedIwantDependencies;

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
				distillery(), distillery2(), docs(), exampleWsdef(), junit(),
				mockWsroot(), testarea(), testrunner(), tutorialWsdefs()));
	}

	// the targets

	private static Path emma() {
		return TestedIwantDependencies.emma();
	}

	private static Target emmaCoverageReport() {
		return EmmaReport
				.with()
				.name("emma-coverage-report")
				.emma(emma())
				.instrumentations(distilleryEmmaInstrumentation(),
						distillery2EmmaInstrumentation(),
						testareaEmmaInstrumentation(),
						testrunnerEmmaInstrumentation())
				.coverages(distillery2EmmaCoverage(), testrunnerEmmaCoverage())
				.end();
	}

	private static EmmaCoverage distillery2EmmaCoverage() {
		return EmmaCoverage
				.with()
				.name("iwant-distillery2.emmacoverage")
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.emma(emma())
				.instrumentations(distilleryEmmaInstrumentation(),
						distillery2EmmaInstrumentation(),
						testareaEmmaInstrumentation())
				.nonInstrumentedClasses(
						distilleryClasspathMarker().mainArtifact(),
						distillery2().testArtifact(), junit().mainArtifact(),
						testarea().mainArtifact(),
						testareaClassdir().mainArtifact(),
						testrunner().testArtifact())
				.mainClassAndArguments("org.junit.runner.JUnitCore",
						"net.sf.iwant.IwantDistillery2Suite").end();
	}

	private static EmmaInstrumentation distilleryEmmaInstrumentation() {
		JavaSrcModule mod = distillery();
		return EmmaInstrumentation.of(
				new JavaClassesAndSources(mod.mainArtifact(), mod
						.mainJavasAsPaths())).using(emma());
	}

	private static EmmaInstrumentation distillery2EmmaInstrumentation() {
		JavaSrcModule mod = distillery2();
		return EmmaInstrumentation.of(
				new JavaClassesAndSources(mod.mainArtifact(), mod
						.mainJavasAsPaths())).using(emma());
	}

	private static EmmaCoverage testrunnerEmmaCoverage() {
		return EmmaCoverage
				.with()
				.name("iwant-testrunner.emmacoverage")
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.emma(emma())
				.instrumentations(testrunnerEmmaInstrumentation())
				.nonInstrumentedClasses(junit().mainArtifact(),
						testrunner().testArtifact())
				.mainClassAndArguments("org.junit.runner.JUnitCore",
						"net.sf.iwant.testrunner.IwantTestRunnerTest").end();
	}

	private static EmmaInstrumentation testareaEmmaInstrumentation() {
		JavaSrcModule mod = testarea();
		return EmmaInstrumentation.of(
				new JavaClassesAndSources(mod.mainArtifact(), mod
						.mainJavasAsPaths())).using(emma());
	}

	private static EmmaInstrumentation testrunnerEmmaInstrumentation() {
		JavaSrcModule mod = testrunner();
		return EmmaInstrumentation.of(
				new JavaClassesAndSources(mod.mainArtifact(), mod
						.mainJavasAsPaths())).using(emma());
	}

	// the modules

	private static JavaModule commonsMath() {
		return JavaBinModule.providing(FromRepository.ibiblio()
				.group("commons-math").name("commons-math").version("1.2"));
	}

	private static JavaSrcModule distillery() {
		return iwantSrcModule("distillery")
				.mainJava("as-some-developer/with/java")
				.mainDeps(junit(), testarea())
				.testDeps(distilleryClasspathMarker()).end();
	}

	private static JavaBinModule distilleryClasspathMarker() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-distillery/classpath-marker"));
	}

	private static JavaSrcModule distillery2() {
		return iwantSrcModule("distillery2").mainDeps(distillery())
				.testDeps(junit(), testarea()).end();
	}

	private static JavaModule docs() {
		return iwantSrcModule("docs").end();
	}

	private static JavaModule exampleWsdef() {
		return iwantSrcModule("example-wsdef").noTestJava()
				.mainDeps(distillery2()).end();
	}

	private static JavaModule junit() {
		return JavaBinModule.providing(FromRepository.ibiblio().group("junit")
				.name("junit").version("4.8.2"));
	}

	private static JavaModule mockWsroot() {
		IwantSrcModuleSpex mod = iwantSrcModule("mock-wsroot").noMainJava()
				.noTestJava();
		mod.mainJava("iwant-distillery/src/main/java");
		mod.mainJava("iwant-testrunner/src/main/java");
		mod.mainJava("iwant-testarea/src/main/java");
		mod.mainJava("iwant-distillery/src/test/java");
		mod.mainJava("iwant-distillery2/src/test/java");
		mod.mainJava("iwant-distillery2/src/main/java");
		mod.mainJava("iwant-distillery/as-some-developer/with/java");
		return mod.mainDeps(junit()).end();
	}

	private static JavaSrcModule testarea() {
		return iwantSrcModule("testarea").noTestJava()
				.mainDeps(testareaClassdir()).end();
	}

	private static JavaBinModule testareaClassdir() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-testarea/testarea-classdir"));
	}

	private static JavaSrcModule testrunner() {
		return iwantSrcModule("testrunner").mainDeps(junit()).end();
	}

	private static JavaModule tutorialWsdefs() {
		return iwantSrcModule("tutorial-wsdefs").noMainJava().noTestJava()
				.mainJava("src").mainDeps(commonsMath(), distillery2()).end();
	}

}
