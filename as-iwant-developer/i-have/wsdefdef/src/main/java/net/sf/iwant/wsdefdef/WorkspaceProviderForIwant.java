package net.sf.iwant.wsdefdef;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;
import org.fluentjava.iwant.core.download.FromRepository;

public class WorkspaceProviderForIwant implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule.with().name("iwant-workspace")
				.locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
				.javaCompliance(JavaCompliance.JAVA_1_8)
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.iwantPlugin().findbugs().withDependencies())
				.mainDeps(ctx.iwantPlugin().jacoco().withDependencies())
				.mainDeps(commonsIo()).end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return "net.sf.iwant.wsdef.WorkspaceFactoryForIwant";
	}

	private static JavaModule commonsIo() {
		return JavaBinModule.providing(FromRepository.repo1MavenOrg()
				.group("commons-io").name("commons-io").version("1.4").jar())
				.end();
	}

}
