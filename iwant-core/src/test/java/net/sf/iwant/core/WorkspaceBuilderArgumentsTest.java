package net.sf.iwant.core;

public class WorkspaceBuilderArgumentsTest extends WorkspaceBuilderTestBase {

	public void testTooFewArguments() {
		try {
			WorkspaceBuilder
					.main(new String[] { EmptyWorkspace.class.getName(),
							wsRoot(), "list-of/targets" });
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

}
