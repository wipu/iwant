package org.fluentjava.iwant.core;

import junit.framework.TestCase;

public class PathDiggerTest{

	private static final Locations LOCATIONS = new Locations("wsRoot",
			"as-someone", "cacheDir", "iwant-libs");

	@Test public void emptyListOfTargets() {
		ContainerPath root = new EmptyWorkspace().wsRoot(LOCATIONS);
		assertEquals("[]", PathDigger.targets(root).toString());
	}

	@Test public void listOfTwoConstantTargetsAmongNonTargetMethods() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);
		assertEquals("[Target:constant2-container/constant2,"
				+ " Target:constantOne]", PathDigger.targets(root).toString());
	}

	@Test public void targetByIllegalName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);
		assertNull(PathDigger.target(root, "illegal"));
	}

	@Test public void targetByName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);

		Target<?> target = PathDigger.target(root,
				"constant2-container/constant2");
		assertEquals("constant2-container/constant2", target.name());
	}

}
