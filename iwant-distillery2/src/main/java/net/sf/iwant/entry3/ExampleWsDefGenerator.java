package net.sf.iwant.entry3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class ExampleWsDefGenerator {

	private static String exampleWorkspaceJava(File iwantWsRoot)
			throws IOException {
		File example = new File(iwantWsRoot,
				"iwant-example-wsdef/src/main/java/"
						+ "com/example/wsdef/Workspace.java");
		return contentOf(example);
	}

	private static String contentOf(File file) throws IOException {
		StringBuilder out = new StringBuilder();
		char[] buffer = new char[8192];
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while (true) {
			int charsRead = reader.read(buffer);
			if (charsRead < 0) {
				break;
			}
			out.append(buffer, 0, charsRead);
		}
		return out.toString();
	}

	static String example(File iwantWsRoot, String newPackage, String newName)
			throws IOException {
		String src = exampleWorkspaceJava(iwantWsRoot);
		return src.replaceFirst("package.*;", "package " + newPackage + ";")
				.replaceFirst("class Workspace ", "class " + newName + " ");
	}

}
