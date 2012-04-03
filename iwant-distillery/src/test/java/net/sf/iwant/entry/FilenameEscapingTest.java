package net.sf.iwant.entry;

import junit.framework.TestCase;

public class FilenameEscapingTest extends TestCase {

	private static void escapeCase(String from, String to) {
		assertEquals(to, Iwant.toSafeFilename(from));
	}

	public void testTrivialCases() {
		escapeCase("", "");
		escapeCase("abc", "abc");
		escapeCase("A1", "A1");
	}

	public void testCharsThatDefinitelyNeedEscaping() {
		escapeCase("a:b", "a%3Ab");
		escapeCase("a/b", "a%2Fb");
		escapeCase("a?b", "a%3Fb");
	}

	public void testNontrivialCharsThatAreSafeAndMoreReadableToLeaveUnescaped() {
		escapeCase("a-b", "a-b");
	}

}
