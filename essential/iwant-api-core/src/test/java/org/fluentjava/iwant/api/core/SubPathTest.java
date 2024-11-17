package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class SubPathTest extends IwantTestCase {

	@Test
	public void parentAndRelativePath() {
		Path parent = Source.underWsroot("parent");
		SubPath s = new SubPath("s", parent, "rel");

		assertSame(parent, s.parent());
		assertEquals("rel", s.relativePath());
	}

	@Test
	public void ingredientsAndContentDescriptor() {
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

	@Test
	public void nonDirectorySubPathAsPath() throws Exception {
		wsRootHasFile("parent/file", "file content");
		Source parent = Source.underWsroot("parent");

		Target target = new SubPath("s", parent, "file");
		target.path(ctx);

		assertEquals("file content", contentOfCached(target));
	}

	@Test
	public void directorySubPathAsPath() throws Exception {
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
	@Test
	public void cachedPathPointsDirectlyUnderOriginal() {
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
	@Test
	public void deletionOfCachedFileIsNotRequested() {
		assertFalse(new SubPath("s", Source.underWsroot("parent"), "sub")
				.expectsCachedTargetMissingBeforeRefresh());
	}

}
