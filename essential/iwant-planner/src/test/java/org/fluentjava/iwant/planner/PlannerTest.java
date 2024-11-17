package org.fluentjava.iwant.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;
import org.fluentjava.iwant.plannermocks.ResourceMock;
import org.fluentjava.iwant.plannermocks.TaskMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlannerTest {

	Planner planner;

	@BeforeEach
	public void before() {
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

	@Test
	public void ingredientlessCleanTask() {
		TaskMock root = TaskMock.named("root").clean().noDeps();

		ensureFresh(root, 2);

		buildEndsSuccessfully();
	}

	@Test
	public void ingredientlessDirtyTask() {
		TaskMock root = TaskMock.named("root").dirty().noDeps();

		ensureFresh(root, 2);

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	// one ingredient

	@Test
	public void cleanTaskWithCleanDep() {
		TaskMock dep = TaskMock.named("dep").clean().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep);

		ensureFresh(root, 2);

		buildEndsSuccessfully();
	}

	@Test
	public void dirtyTaskWithCleanDep() {
		TaskMock dep = TaskMock.named("dep").clean().noDeps();
		TaskMock root = TaskMock.named("root").dirty().deps(dep);

		ensureFresh(root, 2);

		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	@Test
	public void cleanTaskWithDirtyDep() {
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

	@Test
	public void cleanTaskWith2DirtyDeps() {
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

	@Test
	public void cleanTaskWith2DirtyDepsAndFirstDepRefreshFails() {
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

	@Test
	public void twoDirtyTasksUsingSameResourcePoolOfOne() {
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

	@Test
	public void twoDirtyTasksUsingSameResourcePoolOfTwo() {
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

	@Test
	public void nonParallelTaskDoesNotStartRefreshIfAnotherIsRunning() {
		TaskMock dep1 = TaskMock.named("dep1").dirty()
				.doesNotSupportParallelism().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh();
		dep2.shallNotStartRefresh();
		root.shallNotStartRefresh();

		dep1.finishesRefresh();
		dep2.shallEventuallyStartRefresh();
		root.shallNotStartRefresh();

		dep2.finishesRefresh();
		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	@Test
	public void normalTaskDoesNotStartRefreshIfNonParallelTaskIsRunning() {
		TaskMock dep1 = TaskMock.named("dep1").dirty().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty()
				.doesNotSupportParallelism().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);

		ensureFresh(root, 2);

		dep1.shallEventuallyStartRefresh();
		dep2.shallNotStartRefresh();
		root.shallNotStartRefresh();

		dep1.finishesRefresh();
		dep2.shallEventuallyStartRefresh();
		root.shallNotStartRefresh();

		dep2.finishesRefresh();
		root.shallEventuallyStartRefresh();
		root.finishesRefresh();
		buildEndsSuccessfully();
	}

	@Test
	public void taskStartLogMessage() {
		// in practice targets whose dependency target is dirty is clean but
		// still refreshed and this is what it looks like in the log:
		TaskMock clean = TaskMock.named("task1").clean().noDeps();
		planner = new Planner(clean, 1);
		assertEquals("(1/2    TaskMock:task1)",
				planner.taskStartMessage(1, 2, clean));
		// and this is what a target whose src has been modified looks like:
		TaskMock dirty = TaskMock.named("task2").dirty().noDeps();
		planner = new Planner(dirty, 1);
		assertEquals("(2/1 S~ TaskMock:task2)",
				planner.taskStartMessage(2, 1, dirty));
	}

	@Test
	public void taskStartLogMessageDoesNotReEvaluateDirtinessForPerformanceReasons() {
		TaskMock dirty = TaskMock.named("task2").dirty().noDeps();
		planner = new Planner(dirty, 1);
		assertEquals("(2/1 S~ TaskMock:task2)",
				planner.taskStartMessage(2, 1, dirty));

		dirty.changesDirtinessTo(TaskDirtiness.NOT_DIRTY);
		// the message still uses the old state:
		assertEquals("(2/1 S~ TaskMock:task2)",
				planner.taskStartMessage(2, 1, dirty));
	}

	@Test
	public void dirtinessStringForLogMessage() {
		assertEquals("  ", Planner.dirtinessToString(TaskDirtiness.NOT_DIRTY));
		assertEquals("D~", Planner
				.dirtinessToString(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED));
		assertEquals("C!", Planner
				.dirtinessToString(TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING));
		assertEquals("D!", Planner.dirtinessToString(
				TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING));
		assertEquals("S~", Planner.dirtinessToString(
				TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED));
		assertEquals("S!", Planner
				.dirtinessToString(TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING));
		assertEquals("T~", Planner.dirtinessToString(
				TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED));
	}

}
