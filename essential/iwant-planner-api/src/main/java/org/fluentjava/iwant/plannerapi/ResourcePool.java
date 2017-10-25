package org.fluentjava.iwant.plannerapi;

public interface ResourcePool {

	boolean hasFreeResources();

	Resource acquire();

	void release(Resource resource);

}
