package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.JavaBinModule;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;
import net.sf.iwant.api.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;

public class WorkspaceForIwant implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"));
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
		return new TreeSet<JavaModule>(
				Arrays.asList(distillery(), distillery2(), docs(),
						exampleWsdef(), mockWsroot(), testarea()));
	}

	private static JavaModule distillery() {
		return iwantSrcModule("distillery")
				.mainJava("as-some-developer/with/java").mainDeps()
				.testDeps(distilleryClasspathMarker(), junit(), testarea())
				.end();
	}

	private static JavaBinModule distilleryClasspathMarker() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-distillery/classpath-marker"));
	}

	private static JavaModule distillery2() {
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

	private static JavaModule testarea() {
		return iwantSrcModule("testarea").noTestJava()
				.mainDeps(testareaClassdir()).end();
	}

	private static JavaBinModule testareaClassdir() {
		return JavaBinModule.providing(Source
				.underWsroot("iwant-testarea/testarea-classdir"));
	}

}
