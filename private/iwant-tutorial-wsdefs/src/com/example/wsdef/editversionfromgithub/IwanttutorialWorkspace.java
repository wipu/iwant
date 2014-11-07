package com.example.wsdef.editversionfromgithub;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.github.FromGithub;

public class IwanttutorialWorkspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				jouluCode());
	}

	public Target jouluCode() {
		return FromGithub.user("wipu").project("joulu")
				.commit("1e913b69b31b145cebf89f5e7821060ddecc8f38");
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
