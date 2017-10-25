package org.fluentjava.iwant.embedded;

import java.io.File;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.testarea.TestArea;

public class AsEmbeddedIwantUserTest extends TestCase {

	private TestArea testArea;
	private File cacheDir;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
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
