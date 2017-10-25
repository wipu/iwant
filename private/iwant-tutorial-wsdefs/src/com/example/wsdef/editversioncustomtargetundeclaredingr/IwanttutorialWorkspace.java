package com.example.wsdef.editversioncustomtargetundeclaredingr;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	private final Path ingredient1 = new HelloTarget("ingredient1", "12");
	private final Path ingredient2 = new HelloTarget("ingredient2", "345");
	private final Target myTarget = new FileSizeSum("file-size-sum",
			Arrays.asList(ingredient1, ingredient2));

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				myTarget);
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.end());
	}

}
