package org.fluentjava.iwant.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StringFilterByEqualityTest {

	@Test
	public void matchingAndNonMatching() {
		StringFilterByEquality a = new StringFilterByEquality("a");
		assertTrue(a.matches("a"));
		assertFalse(a.matches("b"));

		StringFilterByEquality b = new StringFilterByEquality("b");
		assertTrue(b.matches("b"));
		assertFalse(b.matches("a"));
	}

	@Test
	public void toStringIsReadable() {
		assertEquals("StringFilterByEquality:a",
				new StringFilterByEquality("a").toString());
		assertEquals("StringFilterByEquality:b",
				new StringFilterByEquality("b").toString());
	}

}
