package net.sf.iwant.planner;

import java.util.Collection;
import java.util.Map;

/**
 * Implementing class must define equals and hashCode by name()
 */
public interface Task {

	void refresh(Map<ResourcePool, Resource> allocatedResources);

	TaskDirtiness dirtiness();

	Collection<Task> dependencies();

	String name();

	Collection<ResourcePool> requiredResources();

	boolean supportsParallelism();

}
