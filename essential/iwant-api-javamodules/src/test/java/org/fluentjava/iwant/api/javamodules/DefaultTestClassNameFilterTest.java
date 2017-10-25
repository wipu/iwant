package org.fluentjava.iwant.api.javamodules;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.model.StringFilter;

public class DefaultTestClassNameFilterTest extends TestCase {

	private StringFilter filter;

	@Override
	protected void setUp() throws Exception {
		filter = new DefaultTestClassNameFilter();
	}

	public void testPositives() {
		assertTrue(filter.matches("com.example.NormalTest"));
		assertTrue(filter.matches("org.anotherexample.AnotherNormalTest"));
	}

	public void testNegatives() {
		assertFalse(filter.matches("com.example.TestUtility"));
		assertFalse(filter.matches("com.example.ServiceMock"));
		assertFalse(filter.matches("com.example.NormalTest$1"));
		assertFalse(filter.matches("com.example.NormalTest$SubClassTest"));
		assertFalse(filter.matches("com.example.AbstractUiTest"));
	}

	public void testToStringIsConstantSoItCanBeUsedInTargetContentDefinition() {
		assertEquals("org.fluentjava.iwant.api.javamodules.DefaultTestClassNameFilter",
				filter.toString());
	}

}
