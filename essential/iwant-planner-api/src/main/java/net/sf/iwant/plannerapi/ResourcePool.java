package net.sf.iwant.plannerapi;

public interface ResourcePool {

	boolean hasFreeResources();

	Resource acquire();

	void release(Resource resource);

}
