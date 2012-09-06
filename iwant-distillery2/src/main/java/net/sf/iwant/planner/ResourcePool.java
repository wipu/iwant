package net.sf.iwant.planner;

public interface ResourcePool {

	boolean hasFreeResources();

	Resource acquire();

	void release(Resource resource);

}
