package net.sf.iwant.api.model;

import junit.framework.TestCase;

public class StringFilterByEqualityTest extends TestCase {

	public void testMatchingAndNonMatching() {
		StringFilterByEquality a = new StringFilterByEquality("a");
		assertTrue(a.matches("a"));
		assertFalse(a.matches("b"));

		StringFilterByEquality b = new StringFilterByEquality("b");
		assertTrue(b.matches("b"));
		assertFalse(b.matches("a"));
	}

	public void testToString() {
		assertEquals("StringFilterByEquality:a",
				new StringFilterByEquality("a").toString());
		assertEquals("StringFilterByEquality:b",
				new StringFilterByEquality("b").toString());
	}

}
