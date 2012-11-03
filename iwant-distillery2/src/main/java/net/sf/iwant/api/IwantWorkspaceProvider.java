package net.sf.iwant.api;

public interface IwantWorkspaceProvider {

	JavaModule workspaceModule(JavaModule iwantApiClasses);

	String workspaceClassname();

}
