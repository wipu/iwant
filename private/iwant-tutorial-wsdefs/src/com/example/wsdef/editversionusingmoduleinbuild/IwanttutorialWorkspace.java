package com.example.wsdef.editversionusingmoduleinbuild;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;

import com.example.util.editversionusingmoduleinbuild.ExampleUtil;
import com.example.wsdefdef.editversionusingmoduleinbuild.IwantTutorialWorkspaceProvider;

public class IwanttutorialWorkspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
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
