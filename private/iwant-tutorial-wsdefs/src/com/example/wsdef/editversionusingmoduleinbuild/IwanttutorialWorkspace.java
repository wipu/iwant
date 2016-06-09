package com.example.wsdef.editversionusingmoduleinbuild;

import java.util.Arrays;
import java.util.List;

import com.example.util.editversionusingmoduleinbuild.ExampleUtil;
import com.example.wsdefdef.editversionusingmoduleinbuild.IwantTutorialWorkspaceProvider;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				targetUsingModuleFromSameWs());
	}

	private static Target targetUsingModuleFromSameWs() {
		return new HelloTarget("targetUsingModuleFromSameWs",
				ExampleUtil.capitalize("String made with ExampleUtil."));
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(IwantTutorialWorkspaceProvider.exampleUtil()).end());
	}

}
