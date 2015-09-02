package com.example.wsdefdef.editversionusingmoduleinbuild;

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
				.mainDeps(exampleUtil(), ctx.wsdefdefModule()).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.IwanttutorialWorkspace";
	}

	public static JavaSrcModule exampleUtil() {
		return JavaSrcModule.with().name("example-util")
				.mainJava("src/main/java").end();
	}

}
