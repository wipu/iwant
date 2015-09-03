package net.sf.iwant.api.wsdef;

import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface WorkspaceModuleProvider {

	JavaSrcModule workspaceModule(WorkspaceModuleContext ctx);

	String workspaceFactoryClassname();

}
