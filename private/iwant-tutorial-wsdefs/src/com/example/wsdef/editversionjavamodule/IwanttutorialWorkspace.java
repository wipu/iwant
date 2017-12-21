package com.example.wsdef.editversionjavamodule;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	private final JavaModule junit = JavaBinModule
			.providing(TestedIwantDependencies.junit()).end();
	private final JavaSrcModule exampleHello = JavaSrcModule.with()
			.name("example-hello").mavenLayout().noMainResources()
			.testDeps(junit).end();

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				exampleHelloClasses());
	}

	private Target exampleHelloClasses() {
		return Concatenated.named("hello-classes")
				.unixPathTo(exampleHello.mainArtifact()).string("\n")
				.unixPathTo(exampleHello.testArtifact()).string("\n").end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule(),
						exampleHello)
				.end());
	}

}
