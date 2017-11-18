package org.fluentjava.iwant.entry3;

import java.io.File;

import org.fluentjava.iwant.coreservices.FileUtil;

class ExampleWsDefGenerator {

	private static String exampleJava(File essential, String javaPath) {
		File example = new File(essential,
				"iwant-example-wsdef/src/main/java/" + javaPath);
		return FileUtil.contentAsString(example);
	}

	static String exampleWsdefdef(File essential, String newPackage,
			String newName, String wsName, String wsClassName) {
		String src = exampleJava(essential,
				"com/example/wsdefdef/WorkspaceProvider.java");
		String out = src.replaceFirst("package.*;",
				"package " + newPackage + ";");
		out = out.replaceFirst("class WorkspaceProvider ",
				"class " + newName + " ");
		out = out.replaceAll("WSNAME", wsName);
		out = out.replaceAll("WSDEF", wsClassName + "Factory");
		return out;
	}

	public static String exampleWsdef(File essential, String newPackage,
			String newName) {
		String src = exampleJava(essential,
				"com/example/wsdef/ExampleWorkspaceFactory.java");
		return src.replaceFirst("package.*;", "package " + newPackage + ";")
				.replaceFirst("class ExampleWorkspaceFactory ",
						"class " + newName + "Factory ")
				.replaceFirst("return new ExampleWorkspace\\(",
						"return new " + newName + "(");
	}

	public static String exampleWs(File essential, String newPackage,
			String newName) {
		String src = exampleJava(essential,
				"com/example/wsdef/ExampleWorkspace.java");
		return src.replaceFirst("package.*;", "package " + newPackage + ";")
				.replaceFirst("class ExampleWorkspace ",
						"class " + newName + " ");
	}

	static String proposedWsdefSimpleName(String wsName) {
		String cap = wsName.substring(0, 1).toUpperCase()
				+ wsName.substring(1, wsName.length());
		cap = cap.replaceAll("[^A-Za-z0-9]", "");
		if (Character.isDigit(cap.charAt(0))) {
			cap = "_" + cap;
		}
		return cap + "Workspace";
	}

	static String proposedWsdefPackage(String wsdefdefPackage) {
		String conventionalEnd = ".wsdefdef";
		if (wsdefdefPackage.endsWith(conventionalEnd)) {
			return wsdefdefPackage.replaceAll(conventionalEnd + "$", ".wsdef");
		}
		return wsdefdefPackage + ".wsdef";
	}

}
