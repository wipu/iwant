package com.example.wsdef.editversionv02antgeneratedtarget;

import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.core.ant.AntGenerated;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;

public class IwanttutorialWorkspace implements Workspace {

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant\n"),
				antGenerated());
	}

	private static HelloTarget justATargetUsedByAntScript() {
		return new HelloTarget("justATargetUsedByAntScript",
				"content of a target");
	}

	private static Target antScript() {
		ConcatenatedBuilder xml = Concatenated.named("script");
		xml.string("<project name='hello' default='hello'>\n");
		xml.string("  <target name='hello'>\n");
		xml.string("    <echo message='Refreshing ${iwant-outfile}'/>\n");
		xml.string("    <copy file='")
				.nativePathTo(justATargetUsedByAntScript())
				.string("' tofile='${iwant-outfile}'/>\n");
		xml.string("    <echo file='${iwant-outfile}' append='true'"
				+ " message=' appended by ant.'/>\n");
		xml.string("  </target>\n");
		xml.string("</project>\n");
		return xml.end();
	}

	private static Target antGenerated() {
		final String antGroup = "org/apache/ant";
		final String antVersion = "1.7.1";
		return AntGenerated.with().name("antGenerated")
				.antJars(
						FromRepository.repo1MavenOrg().group(antGroup)
								.name("ant").version(antVersion).jar(),
						FromRepository.repo1MavenOrg().group(antGroup)
								.name("ant-launcher").version(antVersion).jar())
				.script(antScript()).end();
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.end());
	}

}
