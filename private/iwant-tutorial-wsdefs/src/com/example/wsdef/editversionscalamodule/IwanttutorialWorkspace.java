package com.example.wsdef.editversionscalamodule;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.javamodules.ScalaVersion;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.core.javamodules.JavaModules;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	private final ScalaVersion scala = ScalaVersion.latestTested();

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
		final JavaSrcModule hello = srcModule("example-hello").noMainResources()
				.mainDeps(helloUtil).end();
		final JavaSrcModule mixedScalaAndJava = JavaSrcModule.with()
				.name("example-mixedscala").scalaVersion(scala)
				.mainJava("src/main/java").mainScala("src/main/scala").end();

	}

	private final ExampleModules modules = new ExampleModules();

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				classpathStringOfAll(), exampleMixedscalaClasspath());
	}

	private Target classpathStringOfAll() {
		ConcatenatedBuilder cp = Concatenated.named("all-as-cp");
		cp.string(".");
		for (Path jar : JavaModules
				.mainArtifactJarsOf(modules.allSrcModules())) {
			cp.string(File.pathSeparator).nativePathTo(jar);
		}
		return cp.end();
	}

	private Target exampleMixedscalaClasspath() {
		return Concatenated.named("example-mixedscala-classpath")
				.unixPathTo(modules.mixedScalaAndJava.mainArtifact())
				.string(":").unixPathTo(scala.libraryJar()).end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules.allSrcModules()).end());
	}

}
