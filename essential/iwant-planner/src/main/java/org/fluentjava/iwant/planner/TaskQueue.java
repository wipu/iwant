package org.fluentjava.iwant.planner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;

public class TaskQueue {

	private final SortedSet<QueuedTask> queuedTasks = new TreeSet<>();
	private final SortedSet<QueuedTask> stillDirty = new TreeSet<>();
	private final SortedSet<QueuedTask> refreshing = new TreeSet<>();
	private final QueuedTask root;
	private final Map<Task, TaskDirtiness> dirtinessByTask = new HashMap<>();
	private boolean isNonParallelRefreshing = false;

	public TaskQueue(Task rootTask) {
		Iwant.debugLog("TaskQueue", "building queue");
		long t1 = System.currentTimeMillis();

		this.root = queuedTask(rootTask, new HashMap<>());

		for (QueuedTask qt : queuedTasks) {
			qt.tellDependantsIfDirty();
		}
		for (QueuedTask qt : queuedTasks) {
			if (qt.isInitiallyDirectlyOrIndirectlyDirty) {
				stillDirty.add(qt);
			}
		}
		for (QueuedTask qt : queuedTasks) {
			dirtinessByTask.put(qt.task, qt.task.dirtiness());
		}

		long t2 = System.currentTimeMillis();
		Iwant.debugLog("TaskQueue", "queue ready in " + (t2 - t1) + "ms.");
	}

	private QueuedTask queuedTask(Task task, Map<Task, QueuedTask> cache) {
		QueuedTask queued = cache.get(task);
		if (queued == null) {
			queued = new QueuedTask(task);
			cache.put(task, queued);
			for (Task dep : task.dependencies()) {
				QueuedTask queuedDep = queuedTask(dep, cache);
				queued.addDep(queuedDep);
			}
		}
		return queued;
	}

	private class QueuedTask implements Comparable<QueuedTask> {
		private final Task task;
		private final SortedSet<QueuedTask> dependants = new TreeSet<>();
		private Boolean isInitiallyDirectlyOrIndirectlyDirty;
		private final SortedSet<QueuedTask> stillDirtyDependencies = new TreeSet<>();

		QueuedTask(Task task) {
			this.task = task;
			queuedTasks.add(this);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ":" + task;
		}

		@Override
		public int compareTo(QueuedTask o) {
			return task.name().compareTo(o.task.name());
		}

		void tellDependantsIfDirty() {
			if (isInitiallyDirectlyOrIndirectlyDirty != null) {
				// already evaluated our dirtiness
				return;
			}
			isInitiallyDirectlyOrIndirectlyDirty = task.dirtiness().isDirty();
			if (isInitiallyDirectlyOrIndirectlyDirty) {
				for (QueuedTask dependant : dependants) {
					dependant.addDirtyDependency(this);
				}
			}
		}

		void addDirtyDependency(QueuedTask dirtyDep) {
			// it makes us dirty
			isInitiallyDirectlyOrIndirectlyDirty = true;
			stillDirtyDependencies.add(dirtyDep);
			// and it makes our dependants dirty:
			for (QueuedTask dependant : dependants) {
				dependant.addDirtyDependency(this);
			}
		}

		void addDep(QueuedTask dep) {
			dep.dependants.add(this);
		}

		boolean hasStillDirtyDependencies() {
			return !stillDirtyDependencies.isEmpty();
		}

	}

	public TaskDirtiness dirtiness(Task task) {
		return dirtinessByTask.get(task);
	}

	public void markDone(TaskAllocation allocation) {
		TaskAllocationImpl impl = (TaskAllocationImpl) allocation;
		impl.releaseResources();
	}

	public boolean isEmpty() {
		return stillDirty.isEmpty();
	}

	public TaskAllocation next() {
		TaskAllocationImpl next = topmostRefreshable(root);
		if (next == null) {
			return null;
		}
		// don't give to other workers:
		refreshing.add(next.queuedTask);
		return next;
	}

	private TaskAllocationImpl topmostRefreshable(QueuedTask qt) {
		Task task = qt.task;
		if (refreshing.contains(qt)) {
			// the task is already refreshing
			return null;
		}
		if (!task.supportsParallelism() && !refreshing.isEmpty()) {
			// the task cannot run with others:
			return null;
		}
		if (isNonParallelRefreshing) {
			// a non-parallel task does not want disturbances:
			return null;
		}
		if (qt.hasStillDirtyDependencies()) {
			for (QueuedTask dep : qt.stillDirtyDependencies) {
				TaskAllocationImpl depAllocation = topmostRefreshable(dep);
				if (depAllocation != null) {
					return depAllocation;
				}
			}
			// the whole subree is still nonrefreshable
			return null;
		}
		Collection<ResourcePool> resourcePools = task.requiredResources();
		for (ResourcePool resourcePool : resourcePools) {
			if (!resourcePool.hasFreeResources()) {
				// the task can't have all required resources:
				return null;
			}
		}
		if (stillDirty.contains(qt)) {
			return new TaskAllocationImpl(qt, resourcePools);
		}
		// nothing to allocate here
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + root.task;
	}

	private class TaskAllocationImpl implements TaskAllocation {

		private final QueuedTask queuedTask;
		private final Map<ResourcePool, Resource> resourceAllocations = new LinkedHashMap<>();

		TaskAllocationImpl(QueuedTask queuedTask,
				Collection<ResourcePool> resourcePools) {
			this.queuedTask = queuedTask;
			for (ResourcePool resourcePool : resourcePools) {
				Resource resource = resourcePool.acquire();
				// TODO what if a task declares the same pool many times?
				resourceAllocations.put(resourcePool, resource);
			}
			if (!queuedTask.task.supportsParallelism()) {
				isNonParallelRefreshing = true;
			}
		}

		@Override
		public Task task() {
			return queuedTask.task;
		}

		@Override
		public Map<ResourcePool, Resource> allocatedResources() {
			return Collections.unmodifiableMap(resourceAllocations);
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(getClass().getSimpleName());
			b.append("(").append(queuedTask.task);
			b.append(" ").append(resourceAllocations);
			b.append(")");
			return b.toString();
		}

		void releaseResources() {
			boolean wasRefreshing = refreshing.remove(queuedTask);
			if (!wasRefreshing) {
				throw new IllegalStateException(
						"Was not refreshing: " + queuedTask.task);
			}
			boolean wasDirty = stillDirty.remove(queuedTask);
			if (!wasDirty) {
				throw new IllegalStateException(
						"Was not dirty: " + queuedTask.task);
			}
			for (Entry<ResourcePool, Resource> allocation : resourceAllocations
					.entrySet()) {
				ResourcePool resourcePool = allocation.getKey();
				Resource resource = allocation.getValue();
				resourcePool.release(resource);
			}
			if (!queuedTask.task.supportsParallelism()) {
				isNonParallelRefreshing = false;
			}
			for (QueuedTask dependant : queuedTask.dependants) {
				dependant.stillDirtyDependencies.remove(queuedTask);
			}
		}

	}

}
