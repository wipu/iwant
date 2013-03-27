package com.example.wsdefdef;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(JavaModule iwantApiClasses) {
		return JavaSrcModule.with().name("WSNAME-workspace")
				.locationUnderWsRoot("as-WSNAME-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantApiClasses).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

}
