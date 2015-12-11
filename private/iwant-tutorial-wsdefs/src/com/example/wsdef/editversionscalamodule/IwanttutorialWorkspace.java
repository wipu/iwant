package com.example.wsdef.editversionscalamodule;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.ScalaVersion;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	private final ScalaVersion scala = ScalaVersion._2_11_7();
	private final JavaSrcModule mixedScalaAndJava = JavaSrcModule.with()
			.name("example-mixedscala").scalaVersion(scala)
			.mainJava("src/main/java").mainScala("src/main/scala").end();

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				exampleMixedscalaClasspath());
	}

	private Target exampleMixedscalaClasspath() {
		return Concatenated.named("example-mixedscala-classpath")
				.unixPathTo(mixedScalaAndJava.mainArtifact()).string(":")
				.unixPathTo(scala.libraryJar()).end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays
				.asList(EclipseSettings.with().name("eclipse-settings")
						.modules(ctx.wsdefdefJavaModule(),
								ctx.wsdefJavaModule(), mixedScalaAndJava)
				.end());
	}

}
