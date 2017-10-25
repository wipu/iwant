package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Copy;

public class ScriptGeneratedContent implements Content {

	private final Path script;
	private final SortedSet<Path> ingredients = new TreeSet<Path>();

	private ScriptGeneratedContent(Path script) {
		this.script = script;
		ingredients.add(script);
	}

	public static ScriptGeneratedContent of(Path script) {
		return new ScriptGeneratedContent(script);
	}

	@Override
	public SortedSet<Path> ingredients() {
		return ingredients;
	}

	@Override
	public void refresh(RefreshEnvironment refresh) throws Exception {
		File tmpDir = refresh.freshTemporaryDirectory();
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

		Process process = new ProcessBuilder(cmd).directory(tmpDir)
				.redirectErrorStream(true).start();

		// err is redirected to out so we need to stream only out, and we stream
		// it to err.
		// TODO reuse code with PrintPrefixes.multiLineErr:
		InputStream out = process.getInputStream();
		boolean readingOut = true;
		boolean lineStart = true;
		while (readingOut) {
			if (readingOut) {
				int c = out.read();
				if (c < 0) {
					readingOut = false;
				} else {
					if (lineStart) {
						System.err.print(PrintPrefixes.fromSystemProperty()
								.errPrefix());
						lineStart = false;
					}
					System.err.print((char) c);
				}
				if ('\n' == c) {
					lineStart = true;
				}
			}
		}

		int result = process.waitFor();
		if (result > 0) {
			throw new IllegalStateException(
					"Script exited with non-zero status " + result);
		}
	}

	@Override
	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getSimpleName());
		b.append(" {\n");
		b.append("  script:").append(script).append("\n");
		b.append("}\n");
		return b.toString();
	}

	@Override
	public String toString() {
		return definitionDescription();
	}

}
