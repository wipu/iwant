package net.sf.iwant.plannermocks;

import net.sf.iwant.plannerapi.Resource;

public class ResourceMock implements Resource {

	private final String name;

	public ResourceMock(String name) {
		this.name = name;
	}

	public static ResourceMock named(String name) {
		return new ResourceMock(name);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + name;
	}

}
