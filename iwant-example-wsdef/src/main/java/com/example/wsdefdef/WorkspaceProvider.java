package com.example.wsdefdef;

import java.util.Arrays;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaClasses workspaceClasses(Path iwantApiClasses) {
		return new JavaClasses("workspaceClasses", workspaceSrc(),
				Arrays.asList(iwantApiClasses));
	}

	private static Source workspaceSrc() {
		return Source.underWsroot("WSDEF_SRC");
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

}
