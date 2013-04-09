package com.example.wsdef.v03scriptgeneratedtarget;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.AntGenerated;
import net.sf.iwant.api.Concatenated;
import net.sf.iwant.api.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.ScriptGenerated;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.model.Target;

import org.apache.commons.math.fraction.Fraction;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				hello2(), antGenerated(), scriptGenerated());
	}

	private static Target hello2() {
		return new HelloTarget("hello2", "1/2 + 2/4 = "
				+ new Fraction(1, 2).add(new Fraction(2, 4)).intValue());
	}

	private static Target antScript() {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("<project name='hello' default='hello'>\n");
		scriptContent.string("  <target name='hello'>\n");
		scriptContent
				.string("    <echo message='Refreshing ${iwant-outfile}'/>\n");
		scriptContent.string("    <copy file='").pathTo(hello2())
				.string("' tofile='${iwant-outfile}'/>\n");
		scriptContent.string("    <echo file='${iwant-outfile}' append='true'"
				+ " message=' appended by ant.'/>\n");
		scriptContent.string("  </target>\n");
		scriptContent.string("</project>\n");
		return scriptContent.end();
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

	private static Target scriptGenerated() {
		ConcatenatedBuilder script = Concatenated.named("shellScript");
		script.string("#!/bin/bash\n");
		script.string("set -eu\n");
		script.string("DEST=$1\n");
		script.string("echo Running $0\n");
		script.string("echo 'We have a dedicated temporary dir:'\n");
		script.string("pwd\n");
		script.string("echo It is ok to create temporary files\n");
		script.string("touch tmpfile\n");
		script.string("ls -F\n");
		script.string("echo Generating $DEST\n");
		script.string("echo 'Hello from script' > \"$DEST\"\n");

		return ScriptGenerated.named("scriptGenerated").byScript(script.end());
	}

}
