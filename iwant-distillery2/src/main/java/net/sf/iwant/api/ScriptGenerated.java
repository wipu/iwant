package net.sf.iwant.api;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;

public class ScriptGenerated extends Target {

	private final Path script;

	private ScriptGenerated(String name, Path script) {
		super(name);
		this.script = script;
	}

	public static ScriptGeneratedSpex named(String name) {
		return new ScriptGeneratedSpex(name);
	}

	public static class ScriptGeneratedSpex {

		private final String name;

		public ScriptGeneratedSpex(String name) {
			this.name = name;
		}

		public ScriptGenerated byScript(Path script) {
			return new ScriptGenerated(name, script);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File tmpDir = ctx.freshTemporaryDirectory();
		File tmpScript = new File(tmpDir.getCanonicalPath(), "script");

		File scriptSrc = ctx.cached(script);
		FileUtil.copyFile(scriptSrc, tmpScript);
		tmpScript.setExecutable(true);

		String[] cmd = { tmpScript.getCanonicalPath(),
				ctx.cached(this).getCanonicalPath() };

		Iwant.debugLog("ScriptGenerated", scriptSrc, Arrays.toString(cmd));

		Process process = new ProcessBuilder(cmd).directory(tmpDir)
				.redirectErrorStream(true).start();
		InputStream out = process.getInputStream();
		StreamUtil.pipe(out, System.err);

		int result = process.waitFor();
		if (result > 0) {
			throw new Iwant.IwantException(
					"Script exited with non-zero status " + result);
		}
	}

	@Override
	public List<Path> ingredients() {
		return Arrays.asList(script);
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + script;
	}

}
