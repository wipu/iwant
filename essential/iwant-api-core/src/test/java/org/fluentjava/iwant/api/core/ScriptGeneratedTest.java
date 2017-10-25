package org.fluentjava.iwant.api.core;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.ScriptGenerated.ExecutionEnvironment;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.entry.Iwant;

public class ScriptGeneratedTest extends IwantTestCase {

	public void testContentDescriptor() {
		assertEquals(
				"org.fluentjava.iwant.api.core.ScriptGenerated\n" + "i:script:\n"
						+ "  src1.sh\n" + "",
				ScriptGenerated.named("s1")
						.byScript(Source.underWsroot("src1.sh"))
						.contentDescriptor());
		assertEquals(
				"org.fluentjava.iwant.api.core.ScriptGenerated\n" + "i:script:\n"
						+ "  src2.sh\n" + "",
				ScriptGenerated.named("s2")
						.byScript(Source.underWsroot("src2.sh"))
						.contentDescriptor());
	}

	public void testScriptIsTheIngredient() {
		assertEquals("[src1.sh]",
				ScriptGenerated.named("s1")
						.byScript(Source.underWsroot("src1.sh")).ingredients()
						.toString());
		assertEquals("[src2.sh]",
				ScriptGenerated.named("s2")
						.byScript(Source.underWsroot("src2.sh")).ingredients()
						.toString());
	}

	public void testEchoToDestFile() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("#!/bin/bash\n");
		scriptContent.string("set -eu\n");
		scriptContent.string("DEST=$1\n");
		scriptContent.string("echo 'hello from script' > \"$DEST\"\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		ScriptGenerated sg = ScriptGenerated.named("sg").byScript(script);
		sg.path(ctx);

		assertEquals("", out());
		assertEquals("", err());

		assertEquals("hello from script\n", contentOfCached("sg"));
	}

	public void testScriptThatDemonstratesItsEnvironment() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("#!/bin/bash\n");
		scriptContent.string("set -eu\n");
		scriptContent.string("echo \\$0=$0\n");
		scriptContent.string("echo \\$1=$1\n");
		scriptContent.string("echo -n 'cwd='\n");
		scriptContent.string("pwd\n");
		scriptContent.string("echo stderr >&2\n");
		scriptContent.string("DEST=$1\n");
		scriptContent.string("echo 'hello from env demo' > \"$DEST\"\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		ScriptGenerated sg = ScriptGenerated.named("sg").byScript(script);
		sg.path(ctx);

		assertEquals("", out());
		assertEquals("$0=" + unixPathOf(tmpDir) + "/script\n" + "$1="
				+ unixPathOf(cached) + "/sg\ncwd=" + unixPathOf(tmpDir) + "\n"
				+ "stderr\n", err());

		assertEquals("hello from env demo\n", contentOfCached("sg"));
	}

	public void testFailingScript() throws Exception {
		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("#!/bin/bash\n");
		scriptContent.string("set -eu\n");
		scriptContent.string("DEST=$1\n");
		scriptContent.string("echo First generating some content\n");
		scriptContent.string("echo 'hello from failing script' > \"$DEST\"\n");
		scriptContent.string("echo Then failing\n");
		scriptContent.string("exit 2\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		ScriptGenerated sg = ScriptGenerated.named("sg").byScript(script);
		try {
			sg.path(ctx);
			fail();
		} catch (Iwant.IwantException e) {
			assertEquals("Script exited with non-zero status 2",
					e.getMessage());
		}

		assertEquals("", out());
		assertEquals("First generating some content\nThen failing\n", err());

		assertEquals("hello from failing script\n", contentOfCached("sg"));
	}

	public void testExecutionEnvUsesProperWrapperWhenCygwinBashExeExists()
			throws Exception {
		File mockCygwinBashExe = new File("mock-bash.exe");
		ctx.iwant().shallFindCygwinBashExeAt(mockCygwinBashExe);
		ctx.iwant().shallMockWintoySafePaths();

		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("whatever\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		ScriptGenerated sg = ScriptGenerated.named("sg").byScript(script);
		ExecutionEnvironment env = sg.prepareExecutionEnvironment(ctx);

		File wrapper = new File(tmpDir, "script-cygwinwrapper.sh");
		assertEquals(tmpDir, env.dir);
		assertEquals(mockCygwinBashExe, env.executable);
		assertEquals(
				"[only-slashes:" + slashed(wrapper) + ", mock-unix-path:"
						+ slashed(new File(cached, "sg")) + "]",
				Arrays.toString(env.args));

		assertEquals(
				"#!/bin/bash\n" + "SCRIPT=$(cygpath --unix -a '"
						+ new File(tmpDir, "script") + "')\n"
						+ "RUNDIR=$(cygpath --unix -a '" + tmpDir + "')\n"
						+ "cd \"$RUNDIR\"\n" + "\"$SCRIPT\" \"$@\"\n" + "",
				contentOf(wrapper));
	}

	public void testExecutionEnvDoesNotUseWrapperWhenCygwinBashExeDoesNotExist()
			throws Exception {
		ctx.iwant().shallNotFindCygwinBash();
		ctx.iwant().shallMockWintoySafePaths();

		ConcatenatedBuilder scriptContent = Concatenated.named("script");
		scriptContent.string("whatever\n");
		Concatenated script = scriptContent.end();
		script.path(ctx);

		ScriptGenerated sg = ScriptGenerated.named("sg").byScript(script);
		ExecutionEnvironment env = sg.prepareExecutionEnvironment(ctx);

		assertEquals(tmpDir, env.dir);
		assertEquals(new File(tmpDir, "script"), env.executable);
		assertEquals("[mock-unix-path:" + slashed(new File(cached, "sg")) + "]",
				Arrays.toString(env.args));
	}

}
