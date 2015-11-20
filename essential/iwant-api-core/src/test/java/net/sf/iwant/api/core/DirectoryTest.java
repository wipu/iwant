package net.sf.iwant.api.core;

import java.io.File;
import java.util.Arrays;

import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;

public class DirectoryTest extends IwantTestCase {

	public void testContentDescriptorAndIngredientsOfEmpty() {
		Directory dir = Directory.named("empty").end();

		assertEquals("net.sf.iwant.api.core.Directory\n"
				+ "p:fullRelativePath:\n" + "  \n" + "",
				dir.contentDescriptor());
		assertEquals("[]", dir.ingredients().toString());
	}

	public void testContentDescriptorAndIngredientsOfDirWithDirsAndPaths() {
		Source ingr1 = Source.underWsroot("src");
		Target ingr2 = new HelloTarget("hello", "hello content");
		Directory dir = Directory.named("nonempty").dir("empty-sub").end()
				.copyOf(ingr1).dir("nonempty-sub").copyOf(ingr2).end().end();

		assertEquals(
				"net.sf.iwant.api.core.Directory\n" + "p:fullRelativePath:\n"
						+ "  \n" + "p:fullRelativePath:\n" + "  /empty-sub\n"
						+ "i:copy-from:\n" + "  src\n" + "p:copy-as:\n"
						+ "  src\n" + "p:fullRelativePath:\n"
						+ "  /nonempty-sub\n" + "i:copy-from:\n" + "  hello\n"
						+ "p:copy-as:\n" + "  hello\n" + "",
				dir.contentDescriptor());
		assertEquals("[src, hello]", dir.ingredients().toString());
	}

	public void testCorrectCachedContentOfEmpty() throws Exception {
		Directory empty = Directory.named("empty").end();

		empty.path(ctx);

		File cachedEmpty = new File(cached, "empty");
		assertTrue(cachedEmpty.exists());
		assertEquals("[]", Arrays.toString(cachedEmpty.list()));
	}

	public void testCorrectCachedContentOfNonEmpty() throws Exception {
		Source src = Source.underWsroot("src");
		Target hello1 = new HelloTarget("hello1", "hello1 content");
		Target hello2 = new HelloTarget("hello2", "hello2 content");

		wsRootHasFile("src", "src content");
		hello1.path(ctx);
		hello2.path(ctx);

		Directory nonempty = Directory.named("nonempty").dir("empty-sub").end()
				.copyOf(src).dir("nonempty-sub").copyOf(hello1)
				.copyOf(hello2, "hello2-renamed").end().end();
		nonempty.path(ctx);

		assertEquals("src content", contentOfCached("nonempty/src"));
		assertTrue(new File(cached, "nonempty/empty-sub").exists());
		assertEquals("hello1 content",
				contentOfCached("nonempty/nonempty-sub/hello1"));
		assertEquals("hello2 content",
				contentOfCached("nonempty/nonempty-sub/hello2-renamed"));
	}

}
