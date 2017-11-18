package com.example.wsdef.editversiontestng;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.WishDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.core.javamodules.JavaModules;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.jacoco.JacocoDistribution;
import org.fluentjava.iwant.plugin.jacoco.JacocoTargetsOfJavaModules;
import org.fluentjava.iwant.plugin.testng.TestngRunner;

public class IwanttutorialWorkspace implements Workspace {

	private WishDefinitionContext wishDefCtx;

	class ExampleModules extends JavaModules {

		@Override
		protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
			return super.commonSettings(m).testDeps(junit);
		}

		final JavaBinModule asmAll = binModule("org/ow2/asm", "asm-all",
				"5.0.1");
		final JavaBinModule hamcrestCore = binModule("org/hamcrest",
				"hamcrest-core", "1.3");
		final JavaBinModule junit = binModule("junit", "junit", "4.11",
				hamcrestCore);
		final JavaBinModule jcommander = binModule(
				TestedIwantDependencies.jcommander());
		final JavaBinModule testng = binModule(TestedIwantDependencies.testng(),
				jcommander);
		final JavaSrcModule helloUtil = srcModule("example-helloutil")
				.noMainResources().end();
		final JavaSrcModule hello = srcModule("example-hello").noMainResources()
				.mainDeps(helloUtil).end();
		final JavaSrcModule testngUser = srcModule("example-testnguser")
				.noMainResources().noTestResources()
				.testDeps(wishDefCtx.iwantPlugin().testng().withDependencies())
				.testRunner(TestngRunner.INSTANCE).end();

	}

	private ExampleModules modules(WishDefinitionContext ctx) {
		this.wishDefCtx = ctx;
		return new ExampleModules();
	}

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				jacocoReport(ctx), classpathStringOfAll(ctx));
	}

	private Target jacocoReport(TargetDefinitionContext ctx) {
		List<JavaModule> rtMods = new ArrayList<>();
		rtMods.addAll(ctx.iwantPlugin().testng().withDependencies());

		List<Path> rt = new ArrayList<>();
		rt.add(TestedIwantDependencies.antJar());
		rt.add(TestedIwantDependencies.antLauncherJar());
		for (JavaModule mod : rtMods) {
			rt.add(mod.mainArtifact());
		}

		return JacocoTargetsOfJavaModules.with()
				.jacocoWithDeps(jacoco(), modules(ctx).asmAll.mainArtifact())
				.antJars(rt.toArray(new Path[0]))
				.modules(modules(ctx).allSrcModules()).end()
				.jacocoReport("jacoco-report");

	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private Target classpathStringOfAll(WishDefinitionContext ctx) {
		ConcatenatedBuilder cp = Concatenated.named("all-as-cp");
		cp.string(".");
		for (Path jar : JavaModules
				.mainArtifactJarsOf(modules(ctx).allSrcModules())) {
			cp.string(File.pathSeparator).nativePathTo(jar);
		}
		return cp.end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules(ctx).allSrcModules()).end());
	}

}
