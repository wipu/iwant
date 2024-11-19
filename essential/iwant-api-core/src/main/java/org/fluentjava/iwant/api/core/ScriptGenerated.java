package org.fluentjava.iwant.api.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.coreservices.StreamUtil;
import org.fluentjava.iwant.entry.Iwant;

public class ScriptGenerated extends TargetBase {

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
		ExecutionEnvironment env = prepareExecutionEnvironment(ctx);
		execute(env);
	}

	ExecutionEnvironment prepareExecutionEnvironment(
			TargetEvaluationContext ctx) throws IOException {
		File tmpDir = ctx.freshTemporaryDirectory();
		File tmpScript = new File(tmpDir.getCanonicalPath(), "script");

		File scriptSrc = ctx.cached(script);
		FileUtil.copyFile(scriptSrc, tmpScript);
		tmpScript.setExecutable(true);

		List<String> args = new ArrayList<>();
		args.add(ctx.iwant().unixPathOf(ctx.cached(this)));
		return prepareExecutionEnvironment(ctx, tmpDir, tmpScript,
				args.toArray(new String[0]));
	}

	private static ExecutionEnvironment prepareExecutionEnvironment(
			TargetEvaluationContext ctx, File dir, File userExecutable,
			String[] userArgs) throws IOException {
		List<String> args = new ArrayList<>();
		File cygwinBashExe = ctx.iwant().windowsBashExe();
		File executable;
		if (cygwinBashExe != null) {
			Iwant.debugLog("ScriptGenerated",
					"Using wrapper for " + cygwinBashExe);
			executable = cygwinBashExe;

			File wrapper = FileUtil
					.textFileEnsuredToHaveContent(
							new File(dir,
									userExecutable.getName()
											+ "-cygwinwrapper.sh"),
							cygwinBashWrapperFor(userExecutable, dir));
			args.add(ctx.iwant().pathWithoutBackslashes(wrapper));
		} else {
			executable = userExecutable;
		}
		for (String arg : userArgs) {
			args.add(arg);
		}
		Iwant.debugLog("ScriptGenerated", dir, executable, args);
		return new ExecutionEnvironment(dir, executable,
				args.toArray(new String[0]));
	}

	public static class ExecutionEnvironment {

		final File dir;
		final File executable;
		final String[] args;

		public ExecutionEnvironment(File dir, File executable, String[] args) {
			this.dir = dir;
			this.executable = executable;
			this.args = args;
		}

	}

	private static String cygwinBashWrapperFor(File script, File runDir)
			throws IOException {
		StringBuilder sh = new StringBuilder();
		sh.append("#!/bin/bash\n");
		sh.append("SCRIPT=$(cygpath --unix -a '" + script.getCanonicalPath()
				+ "')\n");
		sh.append("RUNDIR=$(cygpath --unix -a '" + runDir.getCanonicalPath()
				+ "')\n");
		sh.append("cd \"$RUNDIR\"\n");
		sh.append("\"$SCRIPT\" \"$@\"\n");
		return sh.toString();
	}

	private static void execute(ExecutionEnvironment env)
			throws IOException, InterruptedException {
		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(env.executable.getCanonicalPath());
		for (String arg : env.args) {
			cmdLine.add(arg);
		}

		execute(env.dir, cmdLine);
	}

	public static void execute(File dir, List<String> cmdLine)
			throws IOException, InterruptedException {
		Process process = new ProcessBuilder(cmdLine).directory(dir)
				.redirectErrorStream(true).start();
		InputStream out = process.getInputStream();
		StreamUtil.pipe(out, System.err);

		int result = process.waitFor();
		if (result > 0) {
			throw new Iwant.IwantException(
					"Script exited with non-zero status " + result);
		}
	}

	public static void execute(TargetEvaluationContext ctx, File dir,
			File executable, String[] args)
			throws IOException, InterruptedException {
		ExecutionEnvironment env = prepareExecutionEnvironment(ctx, dir,
				executable, args);
		execute(env);
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return iUse.ingredients("script", script).nothingElse();
	}

}
