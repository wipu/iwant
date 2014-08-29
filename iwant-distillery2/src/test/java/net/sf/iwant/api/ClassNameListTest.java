package net.sf.iwant.api;

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

		assertEquals("net.sf.iwant.api.ClassNameList {\n"
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

		assertEquals("net.sf.iwant.api.ClassNameList {\n"
				+ "  classes:classes2\n"
				+ "  filter:TestFilter to test descriptor\n}\n",
				list.contentDescriptor());
	}

	public void testAllClassesFromEmptyDirectory() throws Exception {
		testArea.newDir("classes");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("", testArea.contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithOneClassInDefaultPackage()
			throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\n", testArea.contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithClassInManyPackages()
			throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.class", "whatever");
		testArea.hasFile("classes/b/B.class", "whatever");
		testArea.hasFile("classes/c/subc/C.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\nb.B\nc.subc.C\n", testArea.contentOf(ctx.cached(list)));
	}

	public void testNonClassFilesAreExcludedAutomatically() throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.notclass", "whatever");
		testArea.hasFile("classes/b/B1.class", "whatever");
		testArea.hasFile("classes/b/B2.notclass", "whatever");
		testArea.hasFile("classes/c/subc/C1.class", "whatever");
		testArea.hasFile("classes/c/subc/C2.notclass", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("b.B1\nc.subc.C1\n", testArea.contentOf(ctx.cached(list)));
	}

	public void testFilterIsAppliedIfGiven() throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/a/A.class", "whatever");
		testArea.hasFile("classes/a/ATest.class", "whatever");
		testArea.hasFile("classes/a/AbstractATest.class", "whatever");
		testArea.hasFile("classes/b/B.class", "whatever");
		testArea.hasFile("classes/b/BTest.class", "whatever");
		testArea.hasFile("classes/b/AbstractBTest.class", "whatever");

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

		assertEquals("a.ATest\nb.BTest\n", testArea.contentOf(ctx.cached(list)));
	}

}
