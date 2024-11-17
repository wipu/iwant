package org.fluentjava.iwant.core;

public class WorkspaceBuilderArgumentsTest extends WorkspaceBuilderTestBase {

	@Test public void tooFewArguments() {
		try {
			WorkspaceBuilder
					.build(new String[] { EmptyWorkspace.class.getName(),
							wsRoot(), "list-of/targets" });
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

}
