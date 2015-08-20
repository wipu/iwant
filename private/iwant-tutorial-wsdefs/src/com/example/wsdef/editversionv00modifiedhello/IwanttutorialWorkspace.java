package com.example.wsdef.editversionv00modifiedhello;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				hello2());
	}

	private static Target hello2() {
		return new HelloTarget("hello2", "hello from my first target\n");
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays
				.asList(EclipseSettings
						.with()
						.name("eclipse-settings")
						.modules(ctx.wsdefdefJavaModule(),
								ctx.wsdefJavaModule()).end());
	}

}
