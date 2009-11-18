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
		assertEquals("[Target:cacheDir/target/constant2-container/constant2,"
				+ " Target:cacheDir/target/constantOne]", PathDigger.targets(
				root).toString());
	}

	public void testTargetByIllegalName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);
		try {
			PathDigger.target(root, "illegal");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testTargetByName() {
		ContainerPath root = new WorkspaceWithTwoConstantTargetFiles()
				.wsRoot(LOCATIONS);

		Target target = PathDigger
				.target(root, "constant2-container/constant2");
		assertEquals("cacheDir/target/constant2-container/constant2", target
				.name());
	}

}
