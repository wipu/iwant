package org.fluentjava.iwant.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.fluentjava.iwant.planner.TaskAllocation;
import org.fluentjava.iwant.planner.TaskQueue;
import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;
import org.junit.jupiter.api.Test;

public class TaskQueueMicrobencmarkTest {

	private static final Random RND = new Random();

	@Test
	public void refreshingBigRandomTaskTreeWithRandomParallelism() {
		int levelCount = 3;
		int tasksPerLevel = 1000;
		double nonParallelProbability = 0.1D;
		double depProbability = 0.2D;
		double finishProbability = 0.1D;
		int taskDoneCount = 0;

		List<Task> lowerLevelTargets = new ArrayList<>();
		List<Task> currentLevel = new ArrayList<>();
		int globalDepCount = 0;
		for (int level = 0; level < levelCount; level++) {
			currentLevel.clear();
			for (int i = 0; i < tasksPerLevel; i++) {
				T t = new T(level + "-" + i,
						randomBoolean(nonParallelProbability));
				for (Task depCandidate : lowerLevelTargets) {
					if (randomBoolean(depProbability)) {
						t.addDep(depCandidate);
						globalDepCount++;
					}
				}
				currentLevel.add(t);
			}
			lowerLevelTargets.addAll(currentLevel);
		}

		T root = new T("root", true);
		for (Task dep : currentLevel) {
			root.addDep(dep);
		}

		System.err.println("Target count: " + T.targetCount);
		System.err.println("Global dep count: " + globalDepCount);
		long t1 = System.currentTimeMillis();
		TaskQueue queue = new TaskQueue(root);
		logTime("queue creation", t1);

		Set<TaskAllocation> running = new HashSet<>();
		while (!queue.isEmpty()) {
			t1 = System.currentTimeMillis();
			TaskAllocation next = queue.next();
			logTime("next " + next, t1);
			if (next != null) {
				running.add(next);
			}
			Set<TaskAllocation> done = new HashSet<>();
			for (TaskAllocation ta : running) {
				if (randomBoolean(finishProbability)) {
					t1 = System.currentTimeMillis();
					queue.markDone(ta);
					logTime("markDone " + ta, t1);
					taskDoneCount++;
					done.add(ta);
				}
			}
			running.removeAll(done);
		}
		assertEquals(levelCount * tasksPerLevel + 1/* one for root */,
				taskDoneCount);
	}

	private static void logTime(String msg, long t1) {
		long t2 = System.currentTimeMillis();
		System.err.println(msg + " took " + (t2 - t1) + "ms.");
	}

	private static boolean randomBoolean(double probability) {
		double rnd = RND.nextDouble();
		return rnd < probability;
	}

	private static class T implements Task {

		private final String name;
		private final boolean supportsParallelism;
		private final List<Task> deps = new ArrayList<>();
		private static int targetCount = 0;

		public T(String name, boolean supportsParallelism) {
			this.name = name;
			this.supportsParallelism = supportsParallelism;
			targetCount++;
		}

		@Override
		public String toString() {
			return name;
		}

		public void addDep(Task dep) {
			deps.add(dep);
		}

		@Override
		public void refresh(Map<ResourcePool, Resource> allocatedResources) {
			// nothing to do
		}

		@Override
		public TaskDirtiness dirtiness() {
			// assuming this won't affect the logic after refresh
			return TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING;
		}

		@Override
		public Collection<Task> dependencies() {
			return deps;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Collection<ResourcePool> requiredResources() {
			return Collections.emptyList();
		}

		@Override
		public boolean supportsParallelism() {
			return supportsParallelism;
		}

	}

}
