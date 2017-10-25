package com.example.wsdef.editversionfindbugsreport;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.javamodules.JavaClassesAndSources;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.findbugs.FindbugsDistribution;
import org.fluentjava.iwant.plugin.findbugs.FindbugsReport;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				mainJavaFindbugsReportOf(findbugsfodder()));
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(findbugsfodder()).end());
	}

	private static Target mainJavaFindbugsReportOf(JavaSrcModule mod) {
		return FindbugsReport.with()
				.name(mod.name() + "-main-java-findbugs-report")
				.using(FindbugsDistribution.ofVersion("3.0.0"),
						TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.classesToAnalyze(new JavaClassesAndSources(mod.mainArtifact(),
						mod.mainJavasAsPaths()))
				.end();
	}

	private static JavaSrcModule findbugsfodder() {
		return JavaSrcModule.with().name("example-findbugsfodder")
				.mainJava("src").end();
	}

}
