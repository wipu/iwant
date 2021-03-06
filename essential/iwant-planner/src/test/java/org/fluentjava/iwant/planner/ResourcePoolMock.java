package org.fluentjava.iwant.planner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannermocks.ResourceMock;

public class ResourcePoolMock implements ResourcePool {

	private final List<ResourceMock> resources;

	private ResourcePoolMock(List<ResourceMock> resources) {
		this.resources = resources;
	}

	public static ResourcePoolMock of(ResourceMock... resources) {
		return new ResourcePoolMock(new ArrayList<>(Arrays.asList(resources)));
	}

	@Override
	public boolean hasFreeResources() {
		return !resources.isEmpty();
	}

	@Override
	public Resource acquire() {
		return resources.remove(0);
	}

	@Override
	public void release(Resource resource) {
		resources.add((ResourceMock) resource);
	}

}
