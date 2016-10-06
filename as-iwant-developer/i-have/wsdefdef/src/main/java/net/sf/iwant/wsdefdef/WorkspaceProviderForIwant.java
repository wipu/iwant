package net.sf.iwant.wsdefdef;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.wsdef.WorkspaceModuleContext;
import net.sf.iwant.api.wsdef.WorkspaceModuleProvider;
import net.sf.iwant.core.download.FromRepository;

public class WorkspaceProviderForIwant implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule.with().name("iwant-workspace")
				.locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
				.javaCompliance(JavaCompliance.JAVA_1_8)
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.iwantPlugin().findbugs().withDependencies())
				.mainDeps(ctx.iwantPlugin().jacoco().withDependencies())
				.mainDeps(ctx.iwantPlugin().javamodules().withDependencies())
				.mainDeps(commonsIo()).end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return "net.sf.iwant.wsdef.WorkspaceFactoryForIwant";
	}

	private static JavaModule commonsIo() {
		return JavaBinModule.providing(FromRepository.repo1MavenOrg()
				.group("commons-io").name("commons-io").version("1.4")).end();
	}

}
