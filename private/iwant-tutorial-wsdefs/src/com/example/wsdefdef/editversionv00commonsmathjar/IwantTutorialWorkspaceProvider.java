package com.example.wsdefdef.editversionv00commonsmathjar;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleProvider;
import org.fluentjava.iwant.core.download.Downloaded;

public class IwantTutorialWorkspaceProvider implements WorkspaceModuleProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceModuleContext ctx) {
		return JavaSrcModule.with().name("iwant-tutorial-wsdef")
				.locationUnderWsRoot("as-iwant-tutorial-developer/i-have/wsdef")
				.mainJava("src/main/java").mainDeps(ctx.iwantApiModules())
				.mainDeps(JavaBinModule.providing(commonsMathJar()).end(),
						ctx.wsdefdefModule())
				.end();
	}

	@Override
	public String workspaceFactoryClassname() {
		return "com.example.wsdef.IwanttutorialWorkspaceFactory";
	}

	private static Target commonsMathJar() {
		final String v = "1.2";
		return Downloaded.withName("commonsMathJar")
				.url("https://repo1.maven.org/maven2/commons-math/commons-math/"
						+ v + "/commons-math-" + v + ".jar")
				.md5("5d3ce091a67e863549de4493e19df069");
	}

}
