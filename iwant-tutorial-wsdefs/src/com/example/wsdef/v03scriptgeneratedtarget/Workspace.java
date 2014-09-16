package com.example.wsdef.v03scriptgeneratedtarget;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.ScriptGenerated;
import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.IwantWorkspace;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.eclipsesettings.EclipseSettings;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				scriptGenerated());
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
