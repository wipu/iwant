package com.example.wsdef.editversionpmdreport;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.pmd.PmdReport;

public class IwanttutorialWorkspace implements Workspace {

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
