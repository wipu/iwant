package com.example.wsdefdef;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaModule;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaModule workspaceModule(JavaModule iwantApiClasses) {
		return JavaModule.with().name("WSNAME-workspace")
				.locationUnderWsRoot("as-WSNAME-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantApiClasses).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

}
