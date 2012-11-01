package com.example.wsdefdef.v00commonsmathjar;

import java.util.Arrays;

import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.IwantWorkspaceProvider;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;

public class WorkspaceProvider implements IwantWorkspaceProvider {

	@Override
	public JavaClasses workspaceClasses(Path iwantApiClasses) {
		return new JavaClasses("workspaceClasses", workspaceSrc(),
				Arrays.asList(iwantApiClasses, commonsMathJar()));
	}

	private static Source workspaceSrc() {
		return Source.underWsroot("as-distillery-developer/i-have/wsdef"
				+ "/src/main/java/com/example/wsdef");
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
