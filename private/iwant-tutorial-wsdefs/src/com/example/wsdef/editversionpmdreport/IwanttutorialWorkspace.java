package com.example.wsdef.editversionpmdreport;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.pmd.PmdReport;

public class IwanttutorialWorkspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				mainJavaPmdReportOf(pmdfodder()));
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(pmdfodder()).end());
	}

	private static Target mainJavaPmdReportOf(JavaSrcModule mod) {
		return PmdReport.with().name(mod.name() + "-main-java-pmd-report")
				.from(mod.mainJavasAsPaths()).end();
	}

	private static JavaSrcModule pmdfodder() {
		return JavaSrcModule.with().name("example-pmdfodder").mainJava("src")
				.end();
	}

}
