package com.example.wsdefdef.v00antjar;

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
				Arrays.asList(iwantApiClasses, antJar()));
	}

	private static Source workspaceSrc() {
		return Source.underWsroot("as-distillery-developer/i-have/wsdef"
				+ "/com/example/wsdef");
	}

	@Override
	public String workspaceClassname() {
		return "com.example.wsdef.Workspace";
	}

	private static Target antJar() {
		final String v = "1.7.1";
		return Downloaded
				.withName("antJar")
				.url("http://mirrors.ibiblio.org/maven2/org/apache/ant/ant/"
						+ v + "/ant-" + v + ".jar")
				.md5("ef62988c744551fb51f330eaa311bfc0");
	}

}
