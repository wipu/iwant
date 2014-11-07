package com.example.wsdefdef;

import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.wsdef.IwantWorkspaceProvider;
import net.sf.iwant.api.wsdef.WorkspaceDefinitionContext;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx) {
		return JavaSrcModule.with().name("WSNAME-workspace")
				.locationUnderWsRoot("as-WSNAME-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.wsdefdefModule()).end();
	}

	@Override
	public String workspaceClassname() {
		return "WSDEF";
	}

}
