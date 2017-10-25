package org.fluentjava.iwant.tests.targetimplementedinbash;

import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;

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
