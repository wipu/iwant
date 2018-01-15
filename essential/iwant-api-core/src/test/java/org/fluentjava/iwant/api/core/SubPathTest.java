package org.fluentjava.iwant.api.core;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;

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

		assertEquals(
				"org.fluentjava.iwant.api.core.SubPath\n" + "i:parent:\n"
						+ "  parent\n" + "p:relativePath:\n" + "  rel\n" + "",
				s.contentDescriptor().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.SubPath\n" + "i:parent:\n"
						+ "  parent2\n" + "p:relativePath:\n" + "  rel2\n" + "",
				s2.contentDescriptor().toString());
	}

	public void testNonDirectorySubPathAsPath() throws Exception {
		wsRootHasFile("parent/file", "file content");
		Source parent = Source.underWsroot("parent");

		Target target = new SubPath("s", parent, "file");
		target.path(ctx);

		assertEquals("file content", contentOfCached(target));
	}

	public void testDirectorySubPathAsPath() throws Exception {
		wsRootHasFile("parent/subdir/subfile1", "subfile1 content");
		wsRootHasFile("parent/subdir/subfile2", "subfile2 content");
		Source parent = Source.underWsroot("parent");

		Target target = new SubPath("s", parent, "subdir");
		target.path(ctx);

		assertEquals("subfile1 content", contentOfCached(target, "subfile1"));
		assertEquals("subfile2 content", contentOfCached(target, "subfile2"));
	}

	/**
	 * SubPath used to copy but it was unnecessary. This tests no copying
	 * happens.
	 */
	public void testCachedPathPointsDirectlyUnderOriginal() {
		Source source = Source.underWsroot("source");
		assertEquals(wsRoot + "/source", ctx.cached(source).getAbsolutePath());
		assertEquals(wsRoot + "/source/subdir",
				ctx.cached(new SubPath("sourcesub", source, "subdir"))
						.getAbsolutePath());
	}

	/**
	 * This is important since no copying happens: the parent must not be
	 * deleted when refreshing a subpath of it.
	 */
	public void testDeletionOfCachedFileIsNotRequested() {
		assertFalse(new SubPath("s", Source.underWsroot("parent"), "sub")
				.expectsCachedTargetMissingBeforeRefresh());
	}

}
