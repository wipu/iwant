package com.example.wsdef.usingmoduleinbuild;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;

import com.example.util.usingmoduleinbuild.ExampleUtil;
import com.example.wsdefdef.usingmoduleinbuild.WorkspaceProvider;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
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
				.modules(WorkspaceProvider.exampleUtil()).end());
	}

}
