package com.example.wsdefdef;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(JavaModule... iwantApiModules) {
		return JavaSrcModule.with().name("WSNAME-workspace")
				.locationUnderWsRoot("as-WSNAME-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantApiModules).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

}
