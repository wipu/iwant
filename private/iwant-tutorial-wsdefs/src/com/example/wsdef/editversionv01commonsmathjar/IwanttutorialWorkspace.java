package com.example.wsdef.editversionv01commonsmathjar;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;

import org.apache.commons.math.fraction.Fraction;

public class IwanttutorialWorkspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				arithmeticWithExtLib());
	}

	private static Target arithmeticWithExtLib() {
		return new HelloTarget("arithmeticWithExtLib", "1/2 + 2/4 = "
				+ new Fraction(1, 2).add(new Fraction(2, 4)).intValue() + "\n");
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
