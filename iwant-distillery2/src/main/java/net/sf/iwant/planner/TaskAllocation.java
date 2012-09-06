package net.sf.iwant.planner;

import java.util.Map;

public interface TaskAllocation {

	Task task();

	Map<ResourcePool, Resource> allocatedResources();

}
