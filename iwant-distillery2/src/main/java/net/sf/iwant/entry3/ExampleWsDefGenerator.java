package net.sf.iwant.entry3;

import java.io.File;

class ExampleWsDefGenerator {

	private static String exampleJava(File iwantWsRoot, String javaPath) {
		File example = new File(iwantWsRoot,
				"iwant-example-wsdef/src/main/java/" + javaPath);
		return FileUtil.contentAsString(example);
	}

	static String exampleWsdefdef(File iwantWsRoot, String newPackage,
			String newName, String wsName) {
		String src = exampleJava(iwantWsRoot,
				"com/example/wsdefdef/WorkspaceProvider.java");
		String out = src.replaceFirst("package.*;", "package " + newPackage
				+ ";");
		out = out.replaceFirst("class WorkspaceProvider ", "class " + newName
				+ " ");
		out = out.replaceAll("WSNAME", wsName);
		return out;
	}

	public static String exampleWsdef(File iwantWsRoot, String newPackage,
			String newName) {
		String src = exampleJava(iwantWsRoot,
				"com/example/wsdef/Workspace.java");
		return src.replaceFirst("package.*;", "package " + newPackage + ";")
				.replaceFirst("class Workspace ", "class " + newName + " ");
	}

}
