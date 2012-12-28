package net.sf.iwant.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.iwant.entry.Iwant;

public class TaskQueue {

	private final Map<Task, TaskDirtiness> dirtinessByTask = new HashMap<Task, TaskDirtiness>();
	private final Set<Task> stillDirty = new HashSet<Task>();
	private final Set<Task> refreshable = new HashSet<Task>();
	private final Set<Task> refreshing = new HashSet<Task>();
	private final Task rootTask;
	private boolean isNonParallelRefreshing = false;

	public TaskQueue(Task rootTask) {
		Iwant.debugLog("TaskQueue", "building queue");
		long t1 = System.currentTimeMillis();
		this.rootTask = rootTask;
		evaluateDirtinesses(rootTask, dirtinessByTask);
		stillDirty.addAll(findDirties(rootTask, dirtinessByTask));
		refreshable.addAll(refreshable(rootTask, stillDirty));
		long t2 = System.currentTimeMillis();
		Iwant.debugLog("TaskQueue", "queue ready in " + (t2 - t1) + "ms.");
	}

	private static Set<Task> refreshable(Task root, Set<Task> dirty) {
		Set<Task> retval = new HashSet<Task>();
		boolean hasDirtyDeps = false;
		for (Task dep : root.dependencies()) {
			retval.addAll(refreshable(dep, dirty));
			if (dirty.contains(dep)) {
				hasDirtyDeps = true;
			}
		}
		if (!hasDirtyDeps && dirty.contains(root)) {
			retval.add(root);
		}
		return retval;
	}

	public TaskDirtiness dirtiness(Task task) {
		return dirtinessByTask.get(task);
	}

	private static void evaluateDirtinesses(Task task,
			Map<Task, TaskDirtiness> dirtinessByTask) {
		if (dirtinessByTask.containsKey(task)) {
			// already evaluated
			return;
		}
		TaskDirtiness dirtiness = task.dirtiness();
		dirtinessByTask.put(task, dirtiness);
		for (Task dep : task.dependencies()) {
			evaluateDirtinesses(dep, dirtinessByTask);
		}
	}

	private static List<Task> findDirties(Task root,
			Map<Task, TaskDirtiness> dirtinessByTask) {
		List<Task> retval = new ArrayList<Task>();
		List<Task> dirtyDeps = new ArrayList<Task>();
		for (Task dep : root.dependencies()) {
			dirtyDeps.addAll(findDirties(dep, dirtinessByTask));
		}
		retval.addAll(dirtyDeps);
		if (!dirtyDeps.isEmpty() || dirtinessByTask.get(root).isDirty()) {
			retval.add(root);
		}
		return retval;
	}

	private void remove(Task finishedTask) {
		boolean wasDirty = stillDirty.remove(finishedTask);
		boolean wasRefreshing = refreshing.remove(finishedTask);
		if (!wasDirty || !wasRefreshing) {
			throw new IllegalStateException("Unexpected removal: "
					+ finishedTask.name());
		}
		refreshable.clear();
		refreshable.addAll(refreshable(rootTask, stillDirty));
	}

	public void markDone(TaskAllocation allocation) {
		TaskAllocationImpl impl = (TaskAllocationImpl) allocation;
		impl.releaseResources();
	}

	public boolean isEmpty() {
		return stillDirty.isEmpty();
	}

	public TaskAllocation next() {
		TaskAllocation next = topmostRefreshable(rootTask, refreshable);
		if (next == null) {
			return null;
		}
		// don't give to other workers:
		refreshing.add(next.task());
		return next;
	}

	private TaskAllocation topmostRefreshable(Task task, Set<Task> refreshable) {
		if (refreshable.contains(task)) {
			if (refreshing.contains(task)) {
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
			Collection<ResourcePool> resourcePools = task.requiredResources();
			for (ResourcePool resourcePool : resourcePools) {
				if (!resourcePool.hasFreeResources()) {
					// the task can't have all required resources:
					return null;
				}
			}
			return new TaskAllocationImpl(task, resourcePools);
		}
		for (Task dep : task.dependencies()) {
			TaskAllocation sub = topmostRefreshable(dep, refreshable);
			if (sub != null) {
				return sub;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + rootTask;
	}

	private class TaskAllocationImpl implements TaskAllocation {

		private final Task task;
		private final Map<ResourcePool, Resource> allocations = new LinkedHashMap<ResourcePool, Resource>();

		TaskAllocationImpl(Task task, Collection<ResourcePool> resourcePools) {
			this.task = task;
			for (ResourcePool resourcePool : resourcePools) {
				Resource resource = resourcePool.acquire();
				// TODO what if a task declares the same pool many times?
				allocations.put(resourcePool, resource);
			}
			if (!task.supportsParallelism()) {
				isNonParallelRefreshing = true;
			}
		}

		@Override
		public Task task() {
			return task;
		}

		@Override
		public Map<ResourcePool, Resource> allocatedResources() {
			return Collections.unmodifiableMap(allocations);
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(getClass().getSimpleName());
			b.append("(").append(task);
			b.append(" ").append(allocations);
			b.append(")");
			return b.toString();
		}

		void releaseResources() {
			remove(task);
			for (Entry<ResourcePool, Resource> allocation : allocations
					.entrySet()) {
				ResourcePool resourcePool = allocation.getKey();
				Resource resource = allocation.getValue();
				resourcePool.release(resource);
			}
			if (!task.supportsParallelism()) {
				isNonParallelRefreshing = false;
			}
		}

	}

}
