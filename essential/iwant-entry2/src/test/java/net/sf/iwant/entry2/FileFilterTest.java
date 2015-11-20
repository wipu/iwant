package net.sf.iwant.entry2;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;
import net.sf.iwant.testarea.TestArea;

public class FileFilterTest extends TestCase {

	private TestArea testArea;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
	}

	private static void assertResult(Collection<File> actual,
			File... expected) {
		assertEquals(expected.length, actual.size());
		for (File f : expected) {
			assertTrue(actual.contains(f));
		}
	}

	public void testFilesRecursivelyUnderPlainFileIsJustFileItsef() {
		File plain = testArea.hasFile("file", "whatever");

		Collection<File> result = Iwant2
				.plainFilesRecursivelyUnder(Arrays.asList(plain));

		assertResult(result, plain);
	}

	public void testFilesRecursivelyUnderDir() {
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

	public void testFilesByName() {
		File java = testArea.hasFile("A.java", "whatever");
		File nonJava = testArea.hasFile("B.nonjava", "whatever");

		assertResult(
				Iwant2.filesByNameSuffix(Arrays.asList(java, nonJava), ".java"),
				java);
		assertResult(Iwant2.filesByNameSuffix(Arrays.asList(java, nonJava),
				".nonjava"), nonJava);
	}

}
