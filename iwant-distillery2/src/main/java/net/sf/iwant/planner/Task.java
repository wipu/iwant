package net.sf.iwant.planner;

import java.util.Collection;
import java.util.Map;

public interface Task {

	void refresh(Map<ResourcePool, Resource> allocatedResources);

	boolean isDirty();

	Collection<Task> dependencies();

	String name();

	Collection<ResourcePool> requiredResources();

	boolean supportsParallelism();

}
