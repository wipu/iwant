package net.sf.iwant.api;

import junit.framework.TestCase;

public class SourceTest extends TestCase {

	public void testToString() {
		assertEquals("a", Source.underWsroot("a").toString());
		assertEquals("b", Source.underWsroot("b").toString());
	}

}
