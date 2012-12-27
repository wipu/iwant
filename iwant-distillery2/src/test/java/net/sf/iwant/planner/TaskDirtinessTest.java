package net.sf.iwant.planner;

import junit.framework.TestCase;

public class TaskDirtinessTest extends TestCase {

	public void testDirtinessBoolean() {
		assertFalse(TaskDirtiness.NOT_DIRTY.isDirty());
		assertTrue(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED.isDirty());
		assertTrue(TaskDirtiness.DIRTY_NO_CACHED_CONTENT.isDirty());
		assertTrue(TaskDirtiness.DIRTY_NO_CACHED_DESCRIPTOR.isDirty());
		assertTrue(TaskDirtiness.DIRTY_SRC_MODIFIED.isDirty());
	}

}
