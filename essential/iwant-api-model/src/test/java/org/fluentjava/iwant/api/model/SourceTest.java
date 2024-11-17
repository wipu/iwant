package org.fluentjava.iwant.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SourceTest {

	@Test
	public void toStringIsThePath() {
		assertEquals("a", Source.underWsroot("a").toString());
		assertEquals("b", Source.underWsroot("b").toString());
	}

	@Test
	public void itHasNoIngredients() {
		assertTrue(Source.underWsroot("whatever").ingredients().isEmpty());
	}

	@Test
	public void wsrootRelativePath() {
		assertEquals("a", Source.underWsroot("a").wsRootRelativePath());
		assertEquals("a/b", Source.underWsroot("a/b").wsRootRelativePath());
	}

}
