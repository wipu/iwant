package net.sf.iwant.plannerapi;

import junit.framework.TestCase;

public class TaskDirtinessTest extends TestCase {

	public void testDirtinessBoolean() {
		assertFalse(TaskDirtiness.NOT_DIRTY.isDirty());
		assertTrue(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED.isDirty());
		assertTrue(TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED.isDirty());
		assertTrue(TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING.isDirty());
		assertTrue(TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED.isDirty());
	}

}
