package net.sf.iwant.core;

import junit.framework.TestCase;

public class PathDiggerTest extends TestCase {

	private static final Locations LOCATIONS = new Locations("wsRoot",
			"cacheDir");

	public void testEmptyListOfTargets() {
		ContainerPath root = new EmptyWorkspace().wsRoot(LOCATIONS);
		assertEquals("[]", PathDigger.targets(root).toString());
	}

	public void testListOfTwoConstantTargetsAmongNonTargetMethods() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);
		assertEquals("[Target:constant2-container/constant2,"
				+ " Target:constantOne]", PathDigger.targets(root).toString());
	}

	public void testTargetByIllegalName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);
		assertNull(PathDigger.target(root, "illegal"));
	}

	public void testTargetByName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);

		Target<?> target = PathDigger.target(root,
				"constant2-container/constant2");
		assertEquals("constant2-container/constant2", target.name());
	}

}
