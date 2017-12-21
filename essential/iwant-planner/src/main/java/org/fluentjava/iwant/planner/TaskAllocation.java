package org.fluentjava.iwant.planner;

import java.util.Map;

import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannerapi.Task;

public interface TaskAllocation {

	Task task();

	Map<ResourcePool, Resource> allocatedResources();

}
