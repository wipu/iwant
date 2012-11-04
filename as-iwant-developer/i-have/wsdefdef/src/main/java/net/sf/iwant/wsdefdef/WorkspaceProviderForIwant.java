package net.sf.iwant.wsdefdef;

import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaModule;

public class WorkspaceProviderForIwant implements IwantWorkspaceProvider {

    @Override
    public JavaModule workspaceModule(JavaModule iwantApiClasses) {
        return JavaModule.with().name("iwant-workspace")
                .locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
                .mainJava("src/main/java").mainDeps(iwantApiClasses).end();
    }

    @Override
    public String workspaceClassname() {
        return "net.sf.iwant.wsdef.WorkspaceForIwant";
    }

}
