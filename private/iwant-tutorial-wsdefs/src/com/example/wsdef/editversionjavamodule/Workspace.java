package com.example.wsdef.editversionjavamodule;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class Workspace implements IwantWorkspace {

	private final JavaModule junit = JavaBinModule.providing(
			TestedIwantDependencies.junit()).end();
	private final JavaSrcModule exampleHello = JavaSrcModule.with()
			.name("example-hello").mavenLayout().noMainResources()
			.testDeps(junit).end();

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				exampleHelloClasses());
	}

	private Target exampleHelloClasses() {
		return Concatenated.named("hello-classes")
				.pathTo(exampleHello.mainArtifact()).string("\n")
				.pathTo(exampleHello.testArtifact()).string("\n").end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings
				.with()
				.name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule(),
						exampleHello).end());
	}

}
