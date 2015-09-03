package com.example.wsdefdef.editversionfindbugsreport;

import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.wsdef.WorkspaceModuleContext;
import net.sf.iwant.api.wsdef.WorkspaceModuleProvider;

public class IwantTutorialWorkspaceProvider implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule
				.with()
				.name("iwant-tutorial-wsdef")
				.locationUnderWsRoot("as-iwant-tutorial-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.wsdefdefModule())
				.mainDeps(ctx.iwantPlugin().findbugs().withDependencies())
				.end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return "com.example.wsdef.IwanttutorialWorkspaceFactory";
	}

}
