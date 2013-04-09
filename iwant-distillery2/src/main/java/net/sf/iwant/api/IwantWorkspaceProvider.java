package net.sf.iwant.api;

public interface IwantWorkspaceProvider {

	JavaSrcModule workspaceModule(JavaModule... iwantApiModules);

	String workspaceClassname();

}
