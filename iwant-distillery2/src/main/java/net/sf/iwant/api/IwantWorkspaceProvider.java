package net.sf.iwant.api;

public interface IwantWorkspaceProvider {

	JavaSrcModule workspaceModule(JavaModule iwantApiClasses);

	String workspaceClassname();

}
