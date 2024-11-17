package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.fluentjava.iwant.api.core.Directory.DirectoryContentPlease;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.junit.jupiter.api.Test;

public class DirectoryTest extends IwantTestCase {

	@Test
	public void contentDescriptorAndIngredientsOfEmpty() {
		Directory dir = Directory.named("empty").end();

		assertEquals(
				"org.fluentjava.iwant.api.core.Directory\n"
						+ "p:fullRelativePath:\n" + "  \n" + "",
				dir.contentDescriptor());
		assertEquals("[]", dir.ingredients().toString());
	}

	@Test
	public void contentDescriptorAndIngredientsOfDirWithDirsAndPaths() {
		Source ingr1 = Source.underWsroot("src");
		Target ingr2 = new HelloTarget("hello", "hello content");
		Directory dir = Directory.named("nonempty").dir("empty-sub").end()
				.copyOf(ingr1).end().dir("nonempty-sub").copyOf(ingr2)
				.executable(true).end().end().end();

		assertEquals("org.fluentjava.iwant.api.core.Directory\n"
				+ "p:fullRelativePath:\n" + "  \n" + "p:fullRelativePath:\n"
				+ "  /empty-sub\n" + "i:copy-from:\n" + "  src\n"
				+ "p:copy-as:\n" + "  src\n" + "p:executable:\n" + " null\n"
				+ "p:fullRelativePath:\n" + "  /nonempty-sub\n"
				+ "i:copy-from:\n" + "  hello\n" + "p:copy-as:\n" + "  hello\n"
				+ "p:executable:\n" + "  true\n" + "", dir.contentDescriptor());
		assertEquals("[src, hello]", dir.ingredients().toString());
	}

	@Test
	public void correctCachedContentOfEmpty() throws Exception {
		Directory empty = Directory.named("empty").end();

		empty.path(ctx);

		File cachedEmpty = new File(cached, "empty");
		assertTrue(cachedEmpty.exists());
		assertEquals("[]", Arrays.toString(cachedEmpty.list()));
	}

	@Test
	public void correctCachedContentOfNonEmpty() throws Exception {
		Source src = Source.underWsroot("src");
		Target hello1 = new HelloTarget("hello1", "hello1 content");
		Target hello2 = new HelloTarget("hello2", "hello2 content");

		wsRootHasFile("src", "src content");
		hello1.path(ctx);
		hello2.path(ctx);

		Directory nonempty = Directory.named("nonempty").dir("empty-sub").end()
				.copyOf(src).end().dir("nonempty-sub").copyOf(hello1).end()
				.copyOf(hello2).named("hello2-renamed").end().end().end();
		nonempty.path(ctx);

		assertEquals("src content", contentOfCached(nonempty, "src"));
		assertTrue(new File(cached, "nonempty/empty-sub").exists());
		assertEquals("hello1 content",
				contentOfCached(nonempty, "nonempty-sub/hello1"));
		assertEquals("hello2 content",
				contentOfCached(nonempty, "nonempty-sub/hello2-renamed"));
	}

	@Test
	public void copyOfNormalFileIsExecutableOnlyIfDeclaredSo()
			throws Exception {
		Target i1 = new HelloTarget("i1", "i1");
		Target i2 = new HelloTarget("i2", "i2");
		Target i3 = new HelloTarget("i3", "i3");
		i1.path(ctx);
		i2.path(ctx);
		i3.path(ctx);

		DirectoryContentPlease<Directory> root = Directory.named("root");
		root.copyOf(i1);
		root.copyOf(i2).executable(false);
		root.copyOf(i3).executable(true);
		root.end().path(ctx);

		assertFalse(new File(cached, "root/i1").canExecute());
		assertFalse(new File(cached, "root/i2").canExecute());
		assertTrue(new File(cached, "root/i3").canExecute());
	}

	@Test
	public void copyOfDirectoryIsExecutableUnlessToldNotToBe()
			throws Exception {
		Target i1 = Directory.named("i1").end();
		Target i2 = Directory.named("i2").end();
		Target i3 = Directory.named("i3").end();
		i1.path(ctx);
		i2.path(ctx);
		i3.path(ctx);

		DirectoryContentPlease<Directory> root = Directory.named("root");
		root.copyOf(i1);
		root.copyOf(i2).executable(false);
		root.copyOf(i3).executable(true);
		root.end().path(ctx);

		assertTrue(new File(cached, "root/i1").canExecute());
		assertFalse(new File(cached, "root/i2").canExecute());
		assertTrue(new File(cached, "root/i3").canExecute());
	}

}
