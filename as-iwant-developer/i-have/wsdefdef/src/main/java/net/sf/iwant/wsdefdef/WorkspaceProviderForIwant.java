package net.sf.iwant.wsdefdef;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public class WorkspaceProviderForIwant implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(JavaModule... iwantApiModules) {
		return JavaSrcModule.with().name("iwant-workspace")
				.locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(iwantApiModules).end();
	}

	@Override
	public String workspaceClassname() {
		return "net.sf.iwant.wsdef.WorkspaceForIwant";
	}

}
