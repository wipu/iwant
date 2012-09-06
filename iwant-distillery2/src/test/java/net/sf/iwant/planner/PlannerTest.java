package net.sf.iwant.planner;

import java.util.Arrays;

import junit.framework.TestCase;

public class PlannerTest extends TestCase {

	Planner planner;

	@Override
	public void setUp() {
		planner = null;
	}

	private static void log(Object... msg) {
		System.err.println(Arrays.toString(msg));
	}

	private void ensureFresh(Task task, int workerCount) {
		planner = new Planner(task, workerCount);
		planner.start();
	}

	private void buildEndsSuccessfully() {
		log("Planner shall eventually end successfully.");
		planner.join();
	}

	private void buildEndsWithFailure(String message) {
		log("Planner shall eventually end with failure ", message);
		try {
			planner.join();
			fail();
		} catch (IllegalStateException e) {
			assertEquals(message, e.getCause().getMessage());
		}
	}

	// the tests

	// ingredientless

	public void testIngredientlessCleanTask() {
		TaskMock root = TaskMock.named("root").clean().noDeps();

		ensureFresh(root, 2);

		buildEndsSuccessfully();
	}

	public void testIngredientlessDirtyTask() {
		TaskMock root = TaskMock.named("root").dirty().noDeps();

		ensureFresh(root, 2);

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	// one ingredient

	public void testCleanTaskWithCleanDep() {
		TaskMock dep = TaskMock.named("dep").clean().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep);

		ensureFresh(root, 2);

		buildEndsSuccessfully();
	}

	public void testDirtyTaskWithCleanDep() {
		TaskMock dep = TaskMock.named("dep").clean().noDeps();
		TaskMock root = TaskMock.named("root").dirty().deps(dep);

		ensureFresh(root, 2);

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	public void testCleanTaskWithDirtyDep() {
		TaskMock dep = TaskMock.named("dep").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep);

		ensureFresh(root, 2);

		dep.shallEventuallyStartRefresh();
		root.shallNotStartRefresh();
		dep.finishesRefresh();
		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	// 2 ingredients

	public void testCleanTaskWith2DirtyDeps() {
		TaskMock dep1 = TaskMock.named("dep1").dirty().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh();
		dep2.shallEventuallyStartRefresh();
		dep1.finishesRefresh();

		root.shallNotStartRefresh();

		dep2.finishesRefresh();
		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	public void testCleanTaskWith2DirtyDepsAndFirstDepRefreshFails() {
		TaskMock dep1 = TaskMock.named("dep1").dirty().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh();
		dep2.shallEventuallyStartRefresh();
		root.shallNotStartRefresh();

		dep1.failsRefresh("dep1 failure");
		dep2.finishesRefresh();
		buildEndsWithFailure("dep1 failure");
	}

	public void testTwoDirtyTasksUsingSameResourcePoolOfOne() {
		ResourceMock resource = ResourceMock.named("r");
		ResourcePoolMock pool = ResourcePoolMock.of(resource);
		TaskMock dep1 = TaskMock.named("dep1").dirty().uses(pool).noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().uses(pool).noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh(resource);
		dep2.shallNotStartRefresh();
		dep1.finishesRefresh();

		dep2.shallEventuallyStartRefresh(resource);
		dep2.finishesRefresh();

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	public void testTwoDirtyTasksUsingSameResourcePoolOfTwo() {
		ResourceMock resource1 = ResourceMock.named("r1");
		ResourceMock resource2 = ResourceMock.named("r2");
		ResourcePoolMock pool = ResourcePoolMock.of(resource1, resource2);
		TaskMock dep1 = TaskMock.named("dep1").dirty().uses(pool).noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().uses(pool).noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh(resource1);
		dep2.shallEventuallyStartRefresh(resource2);

		dep1.finishesRefresh();
		dep2.finishesRefresh();

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

}
