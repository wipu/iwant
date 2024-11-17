package org.fluentjava.iwant.api.javamodules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.fluentjava.iwant.api.model.StringFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultTestClassNameFilterTest {

	private StringFilter filter;

	@BeforeEach
	protected void before() throws Exception {
		filter = new DefaultTestClassNameFilter();
	}

	@Test
	public void positives() {
		assertTrue(filter.matches("com.example.NormalTest"));
		assertTrue(filter.matches("org.anotherexample.AnotherNormalTest"));
	}

	@Test
	public void negatives() {
		assertFalse(filter.matches("com.example.TestUtility"));
		assertFalse(filter.matches("com.example.ServiceMock"));
		assertFalse(filter.matches("com.example.NormalTest$1"));
		assertFalse(filter.matches("com.example.NormalTest$SubClassTest"));
		assertFalse(filter.matches("com.example.AbstractUiTest"));
	}

	@Test
	public void toStringIsConstantSoItCanBeUsedInTargetContentDefinition() {
		assertEquals(
				"org.fluentjava.iwant.api.javamodules.DefaultTestClassNameFilter",
				filter.toString());
	}

}
