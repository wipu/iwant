package net.sf.iwant.api.wsdef;

import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface IwantWorkspaceProvider {

	JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx);

	String workspaceClassname();

}
