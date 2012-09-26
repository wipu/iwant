package com.example.wsdefdef;

import java.util.Arrays;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public Target workspaceClasses(Path iwantApiClasses) {
		return new JavaClasses("workspaceClasses", workspaceSrc(),
				Arrays.asList(iwantApiClasses));
	}

	private static Source workspaceSrc() {
		return Source.underWsroot("AS_EXAMPLE_DEVELOPER/i-have/wsdef"
				+ "/com/example/wsdef");
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

}
