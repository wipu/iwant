package net.sf.iwant.api;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public interface IwantWorkspaceProvider {

	JavaSrcModule workspaceModule(JavaModule... iwantApiModules);

	String workspaceClassname();

}
