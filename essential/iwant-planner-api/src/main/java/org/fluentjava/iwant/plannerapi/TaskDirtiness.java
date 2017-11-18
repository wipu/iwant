package org.fluentjava.iwant.plannerapi;

public enum TaskDirtiness {

	NOT_DIRTY,

	DIRTY_CACHED_DESCRIPTOR_MISSING,

	DIRTY_DESCRIPTOR_CHANGED,

	DIRTY_CACHED_CONTENT_MISSING,

	DIRTY_SRC_INGREDIENT_MODIFIED,

	DIRTY_SRC_INGREDIENT_MISSING,

	DIRTY_TARGET_INGREDIENT_MODIFIED;

	public boolean isDirty() {
		return NOT_DIRTY != this;
	}

}