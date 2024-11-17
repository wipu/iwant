package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class ConcatenatedTest extends IwantTestCase {

	@Test
	public void ingredients() {
		assertEquals("[]", Concatenated.named("no-paths").bytes(1, 2)
				.string("s").end().ingredients().toString());
		assertEquals("[src, target, target2]",
				Concatenated.named("paths").contentOf(Source.underWsroot("src"))
						.nativePathTo(new HelloTarget("target", ""))
						.unixPathTo(new HelloTarget("target2", "")).end()
						.ingredients().toString());
	}

	@Test
	public void contentDescriptor() {
		assertEquals(
				"org.fluentjava.iwant.api.core.Concatenated\n" + "p:bytes:\n"
						+ "  [1, 2]\n" + "p:string:\n" + "  s\n" + "",
				Concatenated.named("all-but-paths").bytes(1, 2).string("s")
						.end().contentDescriptor());
		assertEquals(
				"org.fluentjava.iwant.api.core.Concatenated\n"
						+ "i:content-of:\n" + "  src\n" + "i:native-path:\n"
						+ "  target\n" + "i:unix-path:\n" + "  target2\n" + "",
				Concatenated.named("only-paths")
						.contentOf(Source.underWsroot("src"))
						.nativePathTo(new HelloTarget("target", ""))
						.unixPathTo(new HelloTarget("target2", "")).end()
						.contentDescriptor());
	}

	@Test
	public void pathToEmpty() throws Exception {
		Concatenated c = Concatenated.named("empty").end();
		c.path(ctx);

		assertEquals("", contentOf(new File(cached, "empty")));
	}

	@Test
	public void pathToConcatenationOfAllKinds() throws Exception {
		wsRootHasFile("src", "src-content");
		Source src = Source.underWsroot("src");
		HelloTarget target = new HelloTarget("target", "target-content");
		target.path(ctx);

		Concatenated c = Concatenated.named("all").bytes('A').bytes('B', '\n')
				.nativePathTo(src).string(":").contentOf(src).string("\n")
				.nativePathTo(target).string(":").contentOf(target).string("\n")
				.line("line").end();
		c.path(ctx);

		assertEquals(
				"AB\n" + slashed(wsRoot) + "/src:src-content\n"
						+ slashed(cached) + "/target:target-content\nline\n",
				contentOf(new File(cached, "all")));
	}

	/**
	 * Handy when generating scripts for cygwin on Windows
	 */
	@Test
	public void unixPathVersusNativePath() throws Exception {
		ctx.iwant().shallMockWintoySafePaths();

		Source src = Source.underWsroot("src");
		Concatenated c = Concatenated.named("paths").string("native:")
				.nativePathTo(src).string("\nunix:").unixPathTo(src)
				.string("\n").end();

		c.path(ctx);

		assertEquals("native:only-slashes:" + slashed(wsRoot)
				+ "/src\nunix:mock-unix-path:" + slashed(wsRoot) + "/src\n",
				contentOfCached(c));
	}

}
