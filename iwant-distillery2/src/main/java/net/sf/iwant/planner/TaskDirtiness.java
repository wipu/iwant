package net.sf.iwant.planner;

public enum TaskDirtiness {

	NOT_DIRTY, DIRTY_NO_CACHED_DESCRIPTOR, DIRTY_DESCRIPTOR_CHANGED, DIRTY_NO_CACHED_CONTENT, DIRTY_SRC_MODIFIED;

	public boolean isDirty() {
		return NOT_DIRTY != this;
	}

}
