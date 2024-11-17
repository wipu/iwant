package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.StringFilter;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class ClassNameListTest extends IwantTestCase {

	@Test
	public void givenClassesIsIngredient() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals("[classes]", list.ingredients().toString());
	}

	@Test
	public void descriptorOfListWithJustClasses() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals(
				"org.fluentjava.iwant.api.core.ClassNameList\n" + "i:classes:\n"
						+ "  classes\n" + "p:filter:\n" + " null\n" + "",
				list.contentDescriptor());
	}

	private static class TestFilter implements StringFilter {

		@Override
		public boolean matches(String candidate) {
			throw new UnsupportedOperationException("Not needed in the test");
		}

		@Override
		public String toString() {
			return "TestFilter to test descriptor";
		}

	}

	@Test
	public void filterIsIncludedInDescriptor() {
		Path classes = Source.underWsroot("classes2");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.matching(new TestFilter()).end();

		assertEquals(
				"org.fluentjava.iwant.api.core.ClassNameList\n" + "i:classes:\n"
						+ "  classes2\n" + "p:filter:\n"
						+ "  TestFilter to test descriptor\n" + "",
				list.contentDescriptor());
	}

	@Test
	public void allClassesFromEmptyDirectory() throws Exception {
		wsRootHasDirectory("classes");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("", contentOf(ctx.cached(list)));
	}

	@Test
	public void allClassesFromDirectoryWithOneClassInDefaultPackage()
			throws Exception {
		wsRootHasFile("classes/A.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\n", contentOf(ctx.cached(list)));
	}

	@Test
	public void allClassesFromDirectoryWithClassInManyPackages()
			throws Exception {
		wsRootHasFile("classes/A.class", "whatever");
		wsRootHasFile("classes/b/B.class", "whatever");
		wsRootHasFile("classes/c/subc/C.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\nb.B\nc.subc.C\n", contentOf(ctx.cached(list)));
	}

	@Test
	public void nonClassFilesAreExcludedAutomatically() throws Exception {
		wsRootHasFile("classes/A.notclass", "whatever");
		wsRootHasFile("classes/b/B1.class", "whatever");
		wsRootHasFile("classes/b/B2.notclass", "whatever");
		wsRootHasFile("classes/c/subc/C1.class", "whatever");
		wsRootHasFile("classes/c/subc/C2.notclass", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("b.B1\nc.subc.C1\n", contentOf(ctx.cached(list)));
	}

	@Test
	public void filterIsAppliedIfGiven() throws Exception {
		wsRootHasFile("classes/a/A.class", "whatever");
		wsRootHasFile("classes/a/ATest.class", "whatever");
		wsRootHasFile("classes/a/AbstractATest.class", "whatever");
		wsRootHasFile("classes/b/B.class", "whatever");
		wsRootHasFile("classes/b/BTest.class", "whatever");
		wsRootHasFile("classes/b/AbstractBTest.class", "whatever");

		StringFilter filter = new StringFilter() {
			@Override
			public boolean matches(String candidate) {
				return candidate.matches(".*Test$")
						&& !candidate.matches(".*Abstract[^.]*Test$");
			}
		};

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).matching(filter).end();
		list.path(ctx);

		assertEquals("a.ATest\nb.BTest\n", contentOf(ctx.cached(list)));
	}

}
