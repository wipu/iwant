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
				"[src, target]",
				Concatenated.named("paths")
						.contentOf(Source.underWsroot("src"))
						.pathTo(new HelloTarget("target", "")).end()
						.ingredients().toString());
	}

	public void testContentDescriptor() {
		assertEquals("Concatenated {\nbytes:[1, 2]\nstring:'s'\n}\n",
				Concatenated.named("all-but-paths").bytes(1, 2).string("s")
						.end().contentDescriptor());
		assertEquals(
				"Concatenated {\ncontent-of:src\npath-of:target\n}\n" + "",
				Concatenated.named("only-paths")
						.contentOf(Source.underWsroot("src"))
						.pathTo(new HelloTarget("target", "")).end()
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
				.pathTo(src).string(":").contentOf(src).string("\n")
				.pathTo(target).string(":").contentOf(target).string("\n")
				.end();
		c.path(ctx);

		assertEquals("AB\n" + wsRoot + "/src:src-content\n" + cacheDir
				+ "/target:target-content\n", contentOf(new File(cacheDir,
				"all")));
	}

}
