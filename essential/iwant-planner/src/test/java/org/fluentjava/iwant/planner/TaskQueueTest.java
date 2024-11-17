package org.fluentjava.iwant.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.plannermocks.ResourceMock;
import org.fluentjava.iwant.plannermocks.TaskMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskQueueTest {

	private TaskQueue queue;

	@BeforeEach
	public void before() {
		queue = null;
	}

	private TaskAllocation nextIs(TaskMock task,
			ResourceMock... allocatedResources) {
		assertFalse(queue.isEmpty());
		TaskAllocation nextAllocation = queue.next();
		assertSame(task, nextAllocation.task());
		assertEquals(Arrays.toString(allocatedResources),
				nextAllocation.allocatedResources().values().toString());
		return nextAllocation;
	}

	private void nextIsNull() {
		assertFalse(queue.isEmpty());
		assertNull(queue.next());
	}

	private void queueIsEmpty() {
		assertTrue(queue.isEmpty());
		assertNull(queue.next());
	}

	private void done(TaskAllocation allocation) {
		queue.markDone(allocation);
	}

	@Test
	public void observableStateOfDeplessClean() {
		Task root = TaskMock.named("clean").clean().noDeps();
		queue = new TaskQueue(root);

		assertTrue(queue.isEmpty());
		assertNull(queue.next());
	}

	@Test
	public void oneDeplessDirty() {
		TaskMock root = TaskMock.named("dirty").dirty().noDeps();
		queue = new TaskQueue(root);

		TaskAllocation rootA = nextIs(root);

		nextIsNull();

		done(rootA);
		queueIsEmpty();
	}

	@Test
	public void cleanTaskWithTwoDirtyDeps() {
		TaskMock dep1 = TaskMock.named("dep1").dirty().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1);
		TaskAllocation dep2A = nextIs(dep2);
		nextIsNull();

		done(dep1A);
		nextIsNull();

		done(dep2A);
		TaskAllocation rootA = nextIs(root);

		done(rootA);

		queueIsEmpty();
	}

	// resources

	@Test
	public void onlyOneTaskRunsAtATimeWhenUsingTheSameSingletonResource() {
		ResourceMock res = ResourceMock.named("r");
		ResourcePoolMock pool = ResourcePoolMock.of(res);
		TaskMock dep1 = TaskMock.named("dep1").dirty().uses(pool).noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().uses(pool).noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1, res);
		// dep2 can't run without the resource:
		nextIsNull();

		done(dep1A);
		TaskAllocation dep2A = nextIs(dep2, res);
		nextIsNull();

		done(dep2A);
		nextIs(root);
		// and so on
	}

	@Test
	public void twoTasksRunsInParallelWhenTheirCommonPoolIsBigEnough() {
		ResourceMock res1 = ResourceMock.named("r1");
		ResourceMock res2 = ResourceMock.named("r2");
		ResourcePoolMock pool = ResourcePoolMock.of(res1, res2);
		TaskMock dep1 = TaskMock.named("dep1").dirty().uses(pool).noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().uses(pool).noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1, res1);
		TaskAllocation dep2A = nextIs(dep2, res2);
		nextIsNull();

		done(dep1A);
		nextIsNull();

		done(dep2A);
		nextIs(root);
		// and so on
	}

	@Test
	public void taskDoesNotReserveAnyResourcesIfItCantGetAll() {
		ResourceMock pool1res = ResourceMock.named("p1r");
		ResourceMock pool2res = ResourceMock.named("p2r");
		ResourcePoolMock pool1 = ResourcePoolMock.of(pool1res);
		ResourcePoolMock pool2 = ResourcePoolMock.of(pool2res);
		TaskMock dep1 = TaskMock.named("dep1").dirty().uses(pool1).noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().uses(pool1, pool2)
				.noDeps();
		TaskMock dep3 = TaskMock.named("dep3").dirty().uses(pool2).noDeps();
		TaskMock root = TaskMock.named("root").clean().uses(pool1, pool2)
				.deps(dep1, dep2, dep3);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1, pool1res);
		// now dep2 can't have pool1res so it shall not reserve pool2res either,
		// so the other user of poo2res, dep3, can start:
		TaskAllocation dep3A = nextIs(dep3, pool2res);
		nextIsNull();
		done(dep1A);
		// dep2 is still missing pool2res:
		nextIsNull();
		done(dep3A);
		// but now it can finally start:
		TaskAllocation dep2A = nextIs(dep2, pool1res, pool2res);
		done(dep2A);
		// finally, all resources are now free for root:
		nextIs(root, pool1res, pool2res);
		// and so on ...
	}

	// non-parallel task

	@Test
	public void nonParallelTaskDoesNotStartRefreshIfAnotherIsRunning() {
		TaskMock dep1 = TaskMock.named("dep1").dirty()
				.doesNotSupportParallelism().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1);
		nextIsNull();

		done(dep1A);
		TaskAllocation dep2A = nextIs(dep2);

		done(dep2A);
		TaskAllocation rootA = nextIs(root);

		done(rootA);

		queueIsEmpty();
	}

	@Test
	public void normalTaskDoesNotStartRefreshIfNonParallelTaskIsRunning() {
		TaskMock dep1 = TaskMock.named("dep1").dirty().noDeps();
		TaskMock dep2 = TaskMock.named("dep2").dirty()
				.doesNotSupportParallelism().noDeps();
		TaskMock root = TaskMock.named("root").clean().deps(dep1, dep2);
		queue = new TaskQueue(root);

		TaskAllocation dep1A = nextIs(dep1);
		nextIsNull();

		done(dep1A);
		TaskAllocation dep2A = nextIs(dep2);

		done(dep2A);
		TaskAllocation rootA = nextIs(root);

		done(rootA);

		queueIsEmpty();
	}

	// other

	@Test
	public void manyInstancesOfTheSameTargetAreHandledAsOne() {
		TaskMock utilCopy1 = TaskMock.named("util").dirty().noDeps();
		TaskMock utilCopy2 = TaskMock.named("util").dirty().noDeps();

		// internal test:
		assertEquals(utilCopy1, utilCopy2);
		assertEquals(utilCopy1.hashCode(), utilCopy2.hashCode());

		TaskMock subModule = TaskMock.named("subModule").dirty()
				.deps(utilCopy1);
		TaskMock root = TaskMock.named("root").clean().deps(subModule,
				utilCopy2);
		queue = new TaskQueue(root);

		TaskAllocation utilA = nextIs(utilCopy1);
		nextIsNull();

		done(utilA);
		TaskAllocation subModuleA = nextIs(subModule);
		nextIsNull();

		done(subModuleA);
		TaskAllocation rootA = nextIs(root);

		done(rootA);
		queueIsEmpty();
	}

}
