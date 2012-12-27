package net.sf.iwant.entry3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class ExampleWsDefGenerator {

	private static String exampleJava(File iwantWsRoot, String javaPath)
			throws IOException {
		File example = new File(iwantWsRoot,
				"iwant-example-wsdef/src/main/java/" + javaPath);
		return contentOf(example);
	}

	private static String contentOf(File file) throws IOException {
		StringBuilder out = new StringBuilder();
		char[] buffer = new char[8192];
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while (true) {
				int charsRead = reader.read(buffer);
				if (charsRead < 0) {
					break;
				}
				out.append(buffer, 0, charsRead);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return out.toString();
	}

	static String exampleWsdefdef(File iwantWsRoot, String newPackage,
			String newName, String wsName) throws IOException {
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
			String newName) throws IOException {
		String src = exampleJava(iwantWsRoot,
				"com/example/wsdef/Workspace.java");
		return src.replaceFirst("package.*;", "package " + newPackage + ";")
				.replaceFirst("class Workspace ", "class " + newName + " ");
	}

}
