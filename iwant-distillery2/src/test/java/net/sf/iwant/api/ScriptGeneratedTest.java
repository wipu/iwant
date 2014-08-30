package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.entry.Iwant;

public class ScriptGeneratedTest extends IwantTestCase {

	public void testContentDescriptor() {
		assertEquals(
				"net.sf.iwant.api.ScriptGenerated:src1.sh",
				ScriptGenerated.named("s1")
						.byScript(Source.underWsroot("src1.sh"))
						.contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.ScriptGenerated:src2.sh",
				ScriptGenerated.named("s2")
						.byScript(Source.underWsroot("src2.sh"))
						.contentDescriptor());
	}

	public void testScriptIsTheIngredient() {
		assertEquals(
				"[src1.sh]",
				ScriptGenerated.named("s1")
						.byScript(Source.underWsroot("src1.sh")).ingredients()
						.toString());
		assertEquals(
				"[src2.sh]",
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

		assertEquals("hello from script\n",
				testArea.contentOf(new File(cacheDir, "sg")));
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
		assertEquals("$0=" + tmpDir + "/script\n" + "$1=" + cacheDir
				+ "/sg\ncwd=" + tmpDir + "\n" + "stderr\n", err());

		assertEquals("hello from env demo\n",
				testArea.contentOf(new File(cacheDir, "sg")));
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
			assertEquals("Script exited with non-zero status 2", e.getMessage());
		}

		assertEquals("", out());
		assertEquals("First generating some content\nThen failing\n", err());

		assertEquals("hello from failing script\n",
				testArea.contentOf(new File(cacheDir, "sg")));
	}

}
