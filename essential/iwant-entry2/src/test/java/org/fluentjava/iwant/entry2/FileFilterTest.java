package org.fluentjava.iwant.entry2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileFilterTest {

	private TestArea testArea;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
	}

	private static void assertResult(Collection<File> actual,
			File... expected) {
		assertEquals(expected.length, actual.size());
		for (File f : expected) {
			assertTrue(actual.contains(f));
		}
	}

	@Test
	public void filesRecursivelyUnderPlainFileIsJustFileItsef() {
		File plain = testArea.hasFile("file", "whatever");

		Collection<File> result = Iwant2
				.plainFilesRecursivelyUnder(Arrays.asList(plain));

		assertResult(result, plain);
	}

	@Test
	public void filesRecursivelyUnderDir() {
		File classes = testArea.newDir("classes");
		File pack1class1 = testArea.hasFile("classes/example/pack1/C1.class",
				"whatever");
		File pack1class2 = testArea.hasFile("classes/example/pack1/C2.class",
				"whatever");
		File pack2class = testArea.hasFile("classes/example/pack2/C.class",
				"whatever");

		Collection<File> result = Iwant2
				.plainFilesRecursivelyUnder(Arrays.asList(classes));

		assertResult(result, pack1class1, pack1class2, pack2class);
	}

	@Test
	public void filesByName() {
		File java = testArea.hasFile("A.java", "whatever");
		File nonJava = testArea.hasFile("B.nonjava", "whatever");

		assertResult(
				Iwant2.filesByNameSuffix(Arrays.asList(java, nonJava), ".java"),
				java);
		assertResult(Iwant2.filesByNameSuffix(Arrays.asList(java, nonJava),
				".nonjava"), nonJava);
	}

}
