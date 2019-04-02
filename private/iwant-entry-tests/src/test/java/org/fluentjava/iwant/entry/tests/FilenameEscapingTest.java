package org.fluentjava.iwant.entry.tests;

import java.io.File;

import org.fluentjava.iwant.entry.Iwant;

import junit.framework.TestCase;

public class FilenameEscapingTest extends TestCase {

	private String origSeparator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		origSeparator = File.separator;
	}

	@Override
	protected void tearDown() throws Exception {
		setSeparator(origSeparator);
		super.tearDown();
	}

	private static void setSeparator(String separator) {
		System.setProperty("file.separator", separator);
	}

	private static void escapeCase(String orig, String onUnixAndOnWindows) {
		escapeCase(orig, onUnixAndOnWindows, onUnixAndOnWindows);
	}

	private static void escapeCase(String orig, String onUnix,
			String onWindows) {
		setSeparator("/");
		assertEquals(onUnix, Iwant.toSafeFilename(orig));
		setSeparator("\\");
		assertEquals(onWindows, Iwant.toSafeFilename(orig));
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
	}

	public void testLoneSlashWithoutParentDirRefIsNotEscaped() {
		escapeCase("a/b", "a/b", "a\\b");
		escapeCase("abc/def/gef", "abc/def/gef", "abc\\def\\gef");
	}

	public void testLoneBackslashWithoutParentDirRefIsNotEscaped() {
		escapeCase("a\\b", "a%5Cb", "a\\b");
		escapeCase("abc\\def\\gef", "abc%5Cdef%5Cgef", "abc\\def\\gef");
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
		escapeCase("//a", "%2F/a", "%2F\\a");
	}

	public void testStartingBackslashIsEscapedButDifferentlyOnWindows() {
		escapeCase("\\a", "%5Ca", "%2Fa");
		escapeCase("\\\\a", "%5C%5Ca", "%2F\\a");
	}

	public void testTwoOrMoreSlashesAreEscaped() {
		escapeCase("a//b", "a/%2Fb", "a\\%2Fb");
		escapeCase("a\\\\b", "a%5C%5Cb", "a\\%2Fb");

		escapeCase("a///b", "a/%2F/b", "a\\%2F\\b");
		escapeCase("a\\\\\\b", "a%5C%5C%5Cb", "a\\%2F\\b");

		escapeCase("a////b", "a/%2F/%2Fb", "a\\%2F\\%2Fb");
		escapeCase("a\\\\\\\\b", "a%5C%5C%5C%5Cb", "a\\%2F\\%2Fb");
	}

	public void testNontrivialCharsThatAreSafeAndMoreReadableToLeaveUnescaped() {
		escapeCase("a-b", "a-b");
		escapeCase("a?b", "a?b");
	}

	public void testARealisticWindowsPathExample() {
		escapeCase("c:\\Documents and Settings\\slave",
				"c%3A%5CDocuments+and+Settings%5Cslave",
				"c%3A\\Documents+and+Settings\\slave");
	}

	public void testARealisticWindowsMingwPathExampleWithMixedSeparators() {
		escapeCase("c:\\Documents and Settings\\and/forward/slashes",
				"c%3A%5CDocuments+and+Settings%5Cand/forward/slashes",
				"c%3A\\Documents+and+Settings\\and\\forward\\slashes");
	}

}
