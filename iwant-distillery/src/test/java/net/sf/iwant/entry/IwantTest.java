package net.sf.iwant.entry;

import junit.framework.TestCase;

public class IwantTest extends TestCase {

	public void testFailure() {
		assertEquals("hello from iwant entry", new Iwant().hello());
	}

}
