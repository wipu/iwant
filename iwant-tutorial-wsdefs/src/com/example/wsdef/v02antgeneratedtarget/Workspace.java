package com.example.wsdef.v02antgeneratedtarget;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.AntGenerated;
import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
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
		xml.string("    <copy file='").pathTo(justATargetUsedByAntScript())
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
		return AntGenerated
				.with()
				.name("antGenerated")
				.antJars(
						FromRepository.ibiblio().group(antGroup).name("ant")
								.version(antVersion),
						FromRepository.ibiblio().group(antGroup)
								.name("ant-launcher").version(antVersion))
				.script(antScript()).end();
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
