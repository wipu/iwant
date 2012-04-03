package net.sf.iwant.entry3;

import junit.framework.TestCase;
import net.sf.iwant.entry.IwantEntryTestArea;
import net.sf.iwant.testarea.IwantTestArea;

/**
 * Mock
 */
public class Iwant3Test extends TestCase {

	public void testHelloFromMockedIwant3() {
		// reference these just like the real thing
		assertNotNull(new IwantEntryTestArea());
		assertNotNull(new IwantTestArea());
		assertEquals("Hello from mocked Iwant3", Iwant3.helloFromMockedIwant3());
	}

}
