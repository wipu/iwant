package net.sf.iwant.api;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.entry3.IwantEntry3TestArea;

public class AsEmbeddedIwantUserTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File cacheDir;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		wsRoot = testArea.newDir("wsRoot");
		cacheDir = testArea.newDir("cached");
	}

	private static void assertFile(File expected, File actual) {
		assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
	}

	public void testHelloAsPath() {
		File cached = AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cacheDir).iwant()
				.target(new HelloTarget("hello", "hello message")).asPath();

		assertFile(new File(cacheDir, "target/hello"), cached);
		assertEquals("hello message", testArea.contentOf(cached));
	}

}
