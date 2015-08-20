package com.example.wsdef.editversioncustomtargetdeclaredparam;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements IwantWorkspace {

	private final Path ingredient1 = new HelloTarget("ingredient1", "12");
	private final Path ingredient2 = new HelloTarget("ingredient2", "345");
	private final Target myTarget = new FileSizeSum("file-size-sum",
			Arrays.asList(ingredient1, ingredient2), "The sum of file sizes");

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				myTarget);
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
