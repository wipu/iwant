package net.sf.iwant.api.core;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.StringFilter;
import net.sf.iwant.apimocks.IwantTestCase;

public class ClassNameListTest extends IwantTestCase {

	public void testGivenClassesIsIngredient() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals("[classes]", list.ingredients().toString());
	}

	public void testDescriptorOfListWithJustClasses() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals("net.sf.iwant.api.core.ClassNameList {\n"
				+ "  classes:classes\n" + "}\n", list.contentDescriptor());
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

	public void testFilterIsIncludedInDescriptor() {
		Path classes = Source.underWsroot("classes2");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.matching(new TestFilter()).end();

		assertEquals("net.sf.iwant.api.core.ClassNameList {\n"
				+ "  classes:classes2\n"
				+ "  filter:TestFilter to test descriptor\n}\n",
				list.contentDescriptor());
	}

	public void testAllClassesFromEmptyDirectory() throws Exception {
		wsRootHasDirectory("classes");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("", contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithOneClassInDefaultPackage()
			throws Exception {
		wsRootHasFile("classes/A.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\n", contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithClassInManyPackages()
			throws Exception {
		wsRootHasFile("classes/A.class", "whatever");
		wsRootHasFile("classes/b/B.class", "whatever");
		wsRootHasFile("classes/c/subc/C.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\nb.B\nc.subc.C\n", contentOf(ctx.cached(list)));
	}

	public void testNonClassFilesAreExcludedAutomatically() throws Exception {
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

	public void testFilterIsAppliedIfGiven() throws Exception {
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
