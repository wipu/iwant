package com.example.wsdef.editversionjavamodules;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.javamodules.JavaModules;

public class IwanttutorialWorkspace implements Workspace {

	class ExampleModules extends JavaModules {

		@Override
		protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
			return super.commonSettings(m).testDeps(junit);
		}

		final JavaBinModule hamcrestCore = binModule("org/hamcrest",
				"hamcrest-core", "1.3");
		final JavaBinModule junit = binModule("junit", "junit", "4.11",
				hamcrestCore);
		final JavaSrcModule helloUtil = srcModule("example-helloutil")
				.noMainResources().end();
		final JavaSrcModule hello = srcModule("example-hello")
				.noMainResources().mainDeps(helloUtil).end();

	}

	private final ExampleModules modules = new ExampleModules();

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				classpathStringOfAll());
	}

	private Target classpathStringOfAll() {
		ConcatenatedBuilder cp = Concatenated.named("all-as-cp");
		cp.string(".");
		for (Path jar : JavaModules.mainArtifactJarsOf(modules.allSrcModules())) {
			cp.string(File.pathSeparator).nativePathTo(jar);
		}
		return cp.end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules.allSrcModules()).end());
	}

}
