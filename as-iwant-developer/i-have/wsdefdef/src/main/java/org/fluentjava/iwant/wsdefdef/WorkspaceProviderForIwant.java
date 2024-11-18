package org.fluentjava.iwant.wsdefdef;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;

public class WorkspaceProviderForIwant implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule.with().name("iwant-workspace")
				.locationUnderWsRoot("as-iwant-developer/i-have/wsdef")
				.javaCompliance(JavaCompliance.of("17"))
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(ctx.wsdefdefModule())
				.mainDeps(ctx.iwantPlugin().findbugs().withDependencies())
				.mainDeps(ctx.iwantPlugin().jacoco().withDependencies())
				.mainDeps(ctx.iwantPlugin().junit5runner().withDependencies())
				.mainDeps(commonsIo()).end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return "org.fluentjava.iwant.wsdef.WorkspaceFactoryForIwant";
	}

	public static JavaModule commonsIo() {
		return JavaBinModule.providing(TestedIwantDependencies.commonsIo())
				.end();
	}

}
