package net.sf.iwant.api;

import java.io.File;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;

public class SubPathTest extends IwantTestCase {

	public void testParentAndRelativePath() {
		Path parent = Source.underWsroot("parent");
		SubPath s = new SubPath("s", parent, "rel");

		assertSame(parent, s.parent());
		assertEquals("rel", s.relativePath());
	}

	public void testIngredientsAndContentDescriptor() {
		Path parent = Source.underWsroot("parent");
		Path parent2 = Source.underWsroot("parent2");
		SubPath s = new SubPath("s", parent, "rel");
		SubPath s2 = new SubPath("s2", parent2, "rel2");

		assertEquals("[parent]", s.ingredients().toString());
		assertEquals("[parent2]", s2.ingredients().toString());

		assertEquals("net.sf.iwant.api.SubPath:parent:rel", s
				.contentDescriptor().toString());
		assertEquals("net.sf.iwant.api.SubPath:parent2:rel2", s2
				.contentDescriptor().toString());
	}

	public void testNonDirectorySubPathAsPath() throws Exception {
		wsRootHasFile("parent/file", "file content");
		Source parent = Source.underWsroot("parent");

		Target target = new SubPath("s", parent, "file");
		target.path(ctx);

		assertEquals("file content",
				testArea.contentOf(new File(cacheDir, "s")));
	}

	public void testDirectorySubPathAsPath() throws Exception {
		wsRootHasFile("parent/subdir/subfile1", "subfile1 content");
		wsRootHasFile("parent/subdir/subfile2", "subfile2 content");
		Source parent = Source.underWsroot("parent");

		Target target = new SubPath("s", parent, "subdir");
		target.path(ctx);

		assertEquals("subfile1 content",
				testArea.contentOf(new File(cacheDir, "s/subfile1")));
		assertEquals("subfile2 content",
				testArea.contentOf(new File(cacheDir, "s/subfile2")));
	}

}
