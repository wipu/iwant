package net.sf.iwant.wsdefdef;

import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;

public class WorkspaceProviderForIwant implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx) {
		return JavaSrcModule.with().name("iwant-workspace")
				.locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(commonsIo()).end();
	}

	@Override
	public String workspaceClassname() {
		return "net.sf.iwant.wsdef.WorkspaceForIwant";
	}

	private static JavaModule commonsIo() {
		return JavaBinModule.providing(
				FromRepository.ibiblio().group("commons-io").name("commons-io")
						.version("1.4")).end();
	}

}
