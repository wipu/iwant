package com.example.wsdefdef.v00commonsmathjar;

import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Target;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(WorkspaceDefinitionContext ctx) {
		return JavaSrcModule
				.with()
				.name("iwant-tutorial-workspace")
				.locationUnderWsRoot("as-iwant-tutorial-developer/i-have/wsdef")
				.mainJava("src/main/java")
				.mainDeps(ctx.iwantApiModules())
				.mainDeps(JavaBinModule.providing(commonsMathJar()),
						ctx.wsdefdefModule()).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

	private static Target commonsMathJar() {
		final String v = "1.2";
		return Downloaded
				.withName("commonsMathJar")
				.url("http://mirrors.ibiblio.org/maven2/commons-math/commons-math/"
						+ v + "/commons-math-" + v + ".jar")
				.md5("5d3ce091a67e863549de4493e19df069");
	}

}
