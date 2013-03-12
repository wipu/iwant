package net.sf.iwant.core;

import junit.framework.TestCase;

public class EmbeddedUsageTest extends TestCase {

	private TestArea testArea;

	@Override
	public void setUp() {
		testArea = TestArea.newEmpty();
	}

	public void testNonexistentTargetAsPath() {
		Workspace ws = Workspace.with().name("test").target("hello")
				.content(Concatenated.from().string("hello world").end())
				.endWorkspace();
		try {
			AsDeveloper.of(ws).at(testArea.asLocations()).iwant()
					.target("nonexistent").asPath();
			fail();
		} catch (Exception e) {
			// expected
		}
	}

	public void testSuccessfulHelloAsPath() throws Exception {
		Workspace ws = Workspace.with().name("test").target("hello")
				.content(Concatenated.from().string("hello world").end())
				.endWorkspace();
		String cachedHello = AsDeveloper.of(ws).at(testArea.asLocations())
				.iwant().target("hello").asPath();
		assertEquals("hello world", TestArea.contentOf(cachedHello));
	}

}
