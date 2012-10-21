package com.example.wsdef.v00modifiedhello;

import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.Target;

public class Workspace implements IwantWorkspace {

	@Override
	public List<? extends Target> targets() {
		return Arrays.asList(new HelloTarget("hello", "hello from iwant"),
				new HelloTarget("hello2", "another target"));
	}

	@Override
	public List<? extends SideEffect> sideEffects() {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.end());
	}

}
