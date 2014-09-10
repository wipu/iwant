package net.sf.iwant.planner;

import java.util.Map;

import net.sf.iwant.plannerapi.Resource;
import net.sf.iwant.plannerapi.ResourcePool;
import net.sf.iwant.plannerapi.Task;

public interface TaskAllocation {

	Task task();

	Map<ResourcePool, Resource> allocatedResources();

}
