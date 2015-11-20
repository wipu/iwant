package net.sf.iwant.api.core;

import java.io.File;

import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.IwantTestCase;

public class ConcatenatedTest extends IwantTestCase {

	public void testIngredients() {
		assertEquals("[]", Concatenated.named("no-paths").bytes(1, 2)
				.string("s").end().ingredients().toString());
		assertEquals("[src, target, target2]",
				Concatenated.named("paths").contentOf(Source.underWsroot("src"))
						.nativePathTo(new HelloTarget("target", ""))
						.unixPathTo(new HelloTarget("target2", "")).end()
						.ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals(
				"net.sf.iwant.api.core.Concatenated\n" + "p:bytes:\n"
						+ "  [1, 2]\n" + "p:string:\n" + "  s\n" + "",
				Concatenated.named("all-but-paths").bytes(1, 2).string("s")
						.end().contentDescriptor());
		assertEquals(
				"net.sf.iwant.api.core.Concatenated\n" + "i:content-of:\n"
						+ "  src\n" + "i:native-path:\n" + "  target\n"
						+ "i:unix-path:\n" + "  target2\n" + "",
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
				.nativePathTo(target).string(":").contentOf(target).string("\n")
				.end();
		c.path(ctx);

		assertEquals(
				"AB\n" + slashed(wsRoot) + "/src:src-content\n"
						+ slashed(cacheDir) + "/target:target-content\n",
				contentOf(new File(cacheDir, "all")));
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

		assertEquals("native:only-slashes:" + slashed(wsRoot)
				+ "/src\nunix:mock-unix-path:" + slashed(wsRoot) + "/src\n",
				contentOfCached("paths"));
	}

}
