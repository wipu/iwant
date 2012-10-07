package net.sf.iwant.api;

public interface IwantWorkspaceProvider {

	JavaClasses workspaceClasses(Path iwantApiClasses);

	String workspaceClassname();

}
