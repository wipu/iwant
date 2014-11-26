package net.sf.iwant.api.core;

import java.io.File;

import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.IwantTestCase;

public class ConcatenatedTest extends IwantTestCase {

	public void testIngredients() {
		assertEquals("[]",
				Concatenated.named("no-paths").bytes(1, 2).string("s").end()
						.ingredients().toString());
		assertEquals(
				"[src, target, target2]",
				Concatenated.named("paths")
						.contentOf(Source.underWsroot("src"))
						.nativePathTo(new HelloTarget("target", ""))
						.unixPathTo(new HelloTarget("target2", "")).end()
						.ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("Concatenated {\nbytes:[1, 2]\nstring:'s'\n}\n",
				Concatenated.named("all-but-paths").bytes(1, 2).string("s")
						.end().contentDescriptor());
		assertEquals(
				"Concatenated {\n" + "content-of:src\n"
						+ "native-path:target\n" + "unix-path:target2\n"
						+ "}\n" + "",
				Concatenated.named("only-paths")
						.contentOf(Source.underWsroot("src"))
						.nativePathTo(new HelloTarget("target", ""))
						.unixPathTo(new HelloTarget("target2", "")).end()
						.contentDescriptor());
	}

	public void testPathToEmpty() throws Exception {
		Concatenated c = Concatenated.named("empty").end();
		c.path(ctx);

		assertEquals("", contentOf(new File(cacheDir, "empty")));
	}

	public void testPathToConcatenationOfAllKinds() throws Exception {
		wsRootHasFile("src", "src-content");
		Source src = Source.underWsroot("src");
		HelloTarget target = new HelloTarget("target", "target-content");
		target.path(ctx);

		Concatenated c = Concatenated.named("all").bytes('A').bytes('B', '\n')
				.nativePathTo(src).string(":").contentOf(src).string("\n")
				.nativePathTo(target).string(":").contentOf(target)
				.string("\n").end();
		c.path(ctx);

		assertEquals("AB\n" + wsRoot + "/src:src-content\n" + cacheDir
				+ "/target:target-content\n", contentOf(new File(cacheDir,
				"all")));
	}

	/**
	 * Handy when generating scripts for cygwin on Windows
	 */
	public void testUnixPathVersusNativePath() throws Exception {
		ctx.iwant().shallMockWintoySafePaths();

		Source src = Source.underWsroot("src");
		Concatenated c = Concatenated.named("paths").string("native:")
				.nativePathTo(src).string("\nunix:").unixPathTo(src)
				.string("\n").end();

		c.path(ctx);

		assertEquals("native:" + wsRoot + "/src\nunix:mock-unix-path:" + wsRoot
				+ "/src\n", contentOfCached("paths"));
	}

}
