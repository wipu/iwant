package com.example.wsdef.editversionfromgithub;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.github.FromGithub;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				jouluCode());
	}

	public Target jouluCode() {
		return FromGithub.user("wipu").project("joulu")
				.commit("1e913b69b31b145cebf89f5e7821060ddecc8f38");
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.end());
	}

}
