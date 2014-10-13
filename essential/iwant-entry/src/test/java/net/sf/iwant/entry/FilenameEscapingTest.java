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
	}

	public void testCharsThatAreMoreRobustEscaped() {
		escapeCase("a b", "a+b");
		escapeCase("a+b", "a%2Bb");
		escapeCase("a'b", "a%27b");
		escapeCase("a\"b", "a%22b");
		escapeCase("a\\b", "a%5Cb");
	}

	public void testLoneSlashWithoutParentDirRefIsNotEscaped() {
		escapeCase("a/b", "a/b");
		escapeCase("abc/def/gef", "abc/def/gef");
	}

	/**
	 * Some safety agains accidents (build tools are omnipotent so no real
	 * security tried here)
	 */
	public void testParentDirRefIsEscaped() {
		escapeCase("../b", "..%2Fb");
		escapeCase("a/..", "a%2F..");
		escapeCase("a/../b", "a%2F..%2Fb");
	}

	public void testStartingSlashIsEscaped() {
		escapeCase("/a", "%2Fa");
		escapeCase("//a", "%2F/a");
	}

	public void testTwoOrMoreSlashesAreEscaped() {
		escapeCase("a//b", "a/%2Fb");
		escapeCase("a///b", "a/%2F/b");
		escapeCase("a////b", "a/%2F/%2Fb");
	}

	public void testNontrivialCharsThatAreSafeAndMoreReadableToLeaveUnescaped() {
		escapeCase("a-b", "a-b");
		escapeCase("a?b", "a?b");
	}

	// and then the real fun, make sure it might even work under cygwin:

	public void testWindowsPathsAreEscaped() {
		escapeCase("c:\\Documents and Settings\\slave",
				"c%3A%5CDocuments+and+Settings%5Cslave");
	}

}
