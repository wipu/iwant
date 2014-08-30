package com.example.wsdef.findbugsreport;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.plugin.findbugs.FindbugsDistribution;
import net.sf.iwant.plugin.findbugs.FindbugsReport;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
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
		return FindbugsReport
				.with()
				.name(mod.name() + "-main-java-findbugs-report")
				.using(FindbugsDistribution.ofVersion("2.0.2"),
						TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.classesToAnalyze(
						new JavaClassesAndSources(mod.mainArtifact(), mod
								.mainJavasAsPaths())).end();
	}

	private static JavaSrcModule findbugsfodder() {
		return JavaSrcModule.with().name("example-findbugsfodder")
				.mainJava("src").end();
	}

}
