package org.fluentjava.iwant.core;

import junit.framework.TestCase;

public class EmbeddedUsageTest{

	private TestArea testArea;

	@Override
	public void setUp() {
		testArea = TestArea.newEmpty();
	}

	@Test public void nonexistentTargetAsPath() {
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

	@Test public void successfulHelloAsPath() throws Exception {
		Workspace ws = Workspace.with().name("test").target("hello")
				.content(Concatenated.from().string("hello world").end())
				.endWorkspace();
		String cachedHello = AsDeveloper.of(ws).at(testArea.asLocations())
				.iwant().target("hello").asPath();
		assertEquals("hello world", TestArea.contentOf(cachedHello));
	}

}
