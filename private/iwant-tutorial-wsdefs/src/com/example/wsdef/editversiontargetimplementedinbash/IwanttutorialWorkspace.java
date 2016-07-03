package com.example.wsdef.editversiontargetimplementedinbash;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.bash.TargetImplementedInBash;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IKnowWhatIAmDoingContext;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		List<Target> t = new ArrayList<>();
		IKnowWhatIAmDoingContext ctx2 = (IKnowWhatIAmDoingContext) ctx;
		File indexSh = new File(ctx2.wsRoot(),
				ctx.wsdefJavaModule().locationUnderWsRoot()
						+ "/src/main/bash/_index.sh");
		t.addAll(TargetImplementedInBash.instancesFrom(ctx2, indexSh));
		return t;
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.end());
	}

}
