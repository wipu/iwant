package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Copy;

public class ScriptGeneratedContent implements Content {

	private final Path script;
	private final SortedSet<Path> ingredients = new TreeSet();

	private ScriptGeneratedContent(Path script) {
		this.script = script;
		ingredients.add(script);
	}

	public static ScriptGeneratedContent of(Path script) {
		return new ScriptGeneratedContent(script);
	}

	public SortedSet<Path> ingredients() {
		return ingredients;
	}

	public void refresh(RefreshEnvironment refresh) throws Exception {
		File tmpDir = refresh.temporaryDirectory();
		File tmpScript = new File(tmpDir.getCanonicalPath() + "/script");

		Project project = new Project();

		Copy copy = new Copy();
		copy.setFile(new File(script.asAbsolutePath(refresh.locations())));
		copy.setTofile(tmpScript);
		PrintStream syserr = System.err;
		try {
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			// Please shut up, Copy
			System.setErr(new PrintStream(err));
			copy.execute();
		} finally {
			System.setErr(syserr);
		}

		Chmod chmod = new Chmod();
		chmod.setProject(project);
		chmod.setFile(tmpScript);
		chmod.setPerm("u+x");
		chmod.execute();

		String[] cmd = { tmpScript.getAbsolutePath(),
				refresh.destination().getAbsolutePath() };
		Process process = Runtime.getRuntime().exec(cmd, null, tmpDir);
		InputStream out = process.getInputStream();
		InputStream err = process.getErrorStream();
		int result = process.waitFor();
		System.err.println("Standard out:");
		print(out);
		System.err.println("Standard err:");
		print(err);
		if (result > 0) {
			throw new IllegalStateException(
					"Script exited with non-zero status " + result);
		}
	}

	private static void print(InputStream in) {
		while (true) {
			try {
				int c = in.read();
				if (c < 0) {
					return;
				}
				System.err.print((char) c);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getSimpleName());
		b.append(" {\n");
		b.append("  script:").append(script).append("\n");
		b.append("}\n");
		return b.toString();
	}

}
