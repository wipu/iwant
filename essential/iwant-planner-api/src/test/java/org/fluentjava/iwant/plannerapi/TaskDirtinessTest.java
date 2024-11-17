package org.fluentjava.iwant.plannerapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TaskDirtinessTest {

	@Test
	public void dirtinessBoolean() {
		assertFalse(TaskDirtiness.NOT_DIRTY.isDirty());
		assertTrue(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED.isDirty());
		assertTrue(TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED.isDirty());
		assertTrue(TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED.isDirty());
	}

}
