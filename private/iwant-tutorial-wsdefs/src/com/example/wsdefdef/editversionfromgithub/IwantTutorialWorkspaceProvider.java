package com.example.wsdefdef.editversionfromgithub;

import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.wsdef.IwantWorkspaceProvider;
import net.sf.iwant.api.wsdef.WorkspaceDefinitionContext;

public class IwantTutorialWorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx) {
		return JavaSrcModule
				.with()
				.name("iwant-tutorial-workspace")
				.locationUnderWsRoot("as-iwant-tutorial-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.wsdefdefModule())
				.mainDeps(ctx.iwantPlugin().github().withDependencies()).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.IwanttutorialWorkspace";
	}

}
