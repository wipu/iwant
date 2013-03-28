package net.sf.iwant.api;

import junit.framework.TestCase;

public class SourceTest extends TestCase {

	public void testToString() {
		assertEquals("a", Source.underWsroot("a").toString());
		assertEquals("b", Source.underWsroot("b").toString());
	}

	public void testItHasNoIngredients() {
		assertTrue(Source.underWsroot("whatever").ingredients().isEmpty());
	}

	public void testWsrootRelativePath() {
		assertEquals("a", Source.underWsroot("a").wsRootRelativePath());
		assertEquals("a/b", Source.underWsroot("a/b").wsRootRelativePath());
	}

}
