package org.fluentjava.iwant.api.wsdef;

import org.fluentjava.iwant.api.javamodules.JavaSrcModule;

public interface WorkspaceModuleProvider {

	JavaSrcModule workspaceModule(WorkspaceModuleContext ctx);

	String workspaceFactoryClassname();

}
