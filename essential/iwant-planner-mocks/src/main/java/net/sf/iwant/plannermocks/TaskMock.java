package net.sf.iwant.plannermocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.iwant.plannerapi.Resource;
import net.sf.iwant.plannerapi.ResourcePool;
import net.sf.iwant.plannerapi.Task;
import net.sf.iwant.plannerapi.TaskDirtiness;

public class TaskMock implements Task {

	private final String name;
	private final List<Task> deps;

	private boolean hasStartedRefresh = false;
	private final Object refreshStartLock = new Object();
	private boolean mayFinishRefresh = false;
	private final Object refreshEndLock = new Object();
	private TaskDirtiness dirtiness;
	private String refreshFailure;
	private final List<ResourcePool> resourcePools;
	private Map<ResourcePool, Resource> allocatedResources;
	private final boolean supportsParallelism;

	public TaskMock(String name, TaskDirtiness dirtiness,
			List<ResourcePool> resourcePools, boolean supportsParallelism,
			Task... deps) {
		this.name = name;
		this.dirtiness = dirtiness;
		this.resourcePools = resourcePools;
		this.supportsParallelism = supportsParallelism;
		this.deps = Arrays.asList(deps);
	}

	public static TaskMockSpex named(String name) {
		return new TaskMockSpex(name);
	}

	private static void log(Object... msg) {
		System.err.println(Arrays.toString(msg));
	}

	public static class TaskMockSpex {

		private TaskDirtiness dirtiness;
		private final String name;
		private List<ResourcePool> resourcePools = new ArrayList<>();
		private boolean supportsParallelism = true;

		public TaskMockSpex(String name) {
			this.name = name;
		}

		public TaskMock noDeps() {
			return deps();
		}

		public TaskMockSpex dirty() {
			dirtiness = TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED;
			return this;
		}

		public TaskMockSpex clean() {
			dirtiness = TaskDirtiness.NOT_DIRTY;
			return this;
		}

		public TaskMock deps(TaskMock... deps) {
			return new TaskMock(name, dirtiness, resourcePools,
					supportsParallelism, deps);
		}

		public TaskMockSpex uses(ResourcePool... resourcePool) {
			resourcePools.addAll(Arrays.asList(resourcePool));
			return this;
		}

		public TaskMockSpex doesNotSupportParallelism() {
			supportsParallelism = false;
			return this;
		}

	}

	public void shallEventuallyStartRefresh(
			ResourceMock... expectedAllocatedResource) {
		log(this, "shall eventually start refresh  with ",
				expectedAllocatedResource);
		synchronized (refreshStartLock) {
			while (!hasStartedRefresh) {
				try {
					refreshStartLock.wait();
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
			Assert.assertEquals(Arrays.toString(expectedAllocatedResource),
					allocatedResources.values().toString());
		}
		log(this, "did eventually start refresh with ",
				expectedAllocatedResource);
	}

	public void shallNotStartRefresh() {
		synchronized (refreshStartLock) {
			if (hasStartedRefresh) {
				throw new IllegalStateException(this
						+ " should not have started refresh.");
			}
		}
	}

	public void finishesRefresh() {
		log(this, " finishes refresh.");
		synchronized (refreshEndLock) {
			mayFinishRefresh = true;
			refreshEndLock.notifyAll();
		}
	}

	public void failsRefresh(String message) {
		log(this, " fails refresh.");
		synchronized (refreshEndLock) {
			mayFinishRefresh = true;
			refreshFailure = message;
			refreshEndLock.notifyAll();
		}
	}

	@Override
	public void refresh(Map<ResourcePool, Resource> allocatedResources) {
		log(this, " starts refresh.");
		synchronized (refreshStartLock) {
			hasStartedRefresh = true;
			this.allocatedResources = allocatedResources;
			refreshStartLock.notifyAll();
		}
		synchronized (refreshEndLock) {
			while (!mayFinishRefresh) {
				log(this, " waits for permission to finish.");
				try {
					refreshEndLock.wait();
				} catch (InterruptedException e) {
					log(e);
					throw new IllegalStateException(e);
				}
			}
			if (refreshFailure != null) {
				throw new IllegalArgumentException(refreshFailure);
			}
		}
		log(this, " finishes refresh.");
	}

	@Override
	public TaskDirtiness dirtiness() {
		return dirtiness;
	}

	public void changesDirtinessTo(TaskDirtiness newDirtiness) {
		this.dirtiness = newDirtiness;
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
	public String toString() {
		return getClass().getSimpleName() + ":" + name;
	}

	@Override
	public Collection<ResourcePool> requiredResources() {
		return resourcePools;
	}

	@Override
	public boolean supportsParallelism() {
		return supportsParallelism;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TaskMock other = (TaskMock) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}