package com.example.wsdefdef.v00commonsmathjar;

import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaBinModule;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;
import net.sf.iwant.api.Target;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaSrcModule workspaceModule(JavaModule iwantApiClasses) {
		return JavaSrcModule
				.with()
				.name("distillery-workspace")
				.locationUnderWsRoot("as-distillery-developer/i-have/wsdef")
				.mainJava("src/main/java")
				.mainDeps(iwantApiClasses,
						JavaBinModule.providing(commonsMathJar())).end();
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

	private static Target commonsMathJar() {
		final String v = "1.2";
		return Downloaded
				.withName("antJar")
				.url("http://mirrors.ibiblio.org/maven2/commons-math/commons-math/"
						+ v + "/commons-math-" + v + ".jar")
				.md5("5d3ce091a67e863549de4493e19df069");
	}

}
