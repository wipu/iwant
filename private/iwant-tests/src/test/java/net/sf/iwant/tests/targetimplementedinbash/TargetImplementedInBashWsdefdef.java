package net.sf.iwant.tests.targetimplementedinbash;

import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.wsdef.WorkspaceModuleContext;
import net.sf.iwant.api.wsdef.WorkspaceModuleProvider;

public class TargetImplementedInBashWsdefdef
		implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule.with().name("iwant-test-wsdef")
				.locationUnderWsRoot("as-test-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.wsdefdefModule()).end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return TargetImplementedInBashWsdefdef.class.getPackage().getName()
				+ ".TargetImplementedInBashWsFactory";
	}

}
