package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.fluentjava.iwant.entry.Iwant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilenameEscapingTest {

	private String origSeparator;

	@BeforeEach
	protected void before() throws Exception {
		origSeparator = File.separator;
	}

	@AfterEach
	protected void after() throws Exception {
		setSeparator(origSeparator);
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

	@Test
	public void trivialCases() {
		escapeCase("", "");
		escapeCase("abc", "abc");
		escapeCase("A1", "A1");
	}

	@Test
	public void charsThatDefinitelyNeedEscaping() {
		escapeCase("a:b", "a%3Ab");
	}

	@Test
	public void charsThatAreMoreRobustEscaped() {
		escapeCase("a b", "a+b");
		escapeCase("a+b", "a%2Bb");
		escapeCase("a'b", "a%27b");
		escapeCase("a\"b", "a%22b");
	}

	@Test
	public void loneSlashWithoutParentDirRefIsNotEscaped() {
		escapeCase("a/b", "a/b", "a\\b");
		escapeCase("abc/def/gef", "abc/def/gef", "abc\\def\\gef");
	}

	@Test
	public void loneBackslashWithoutParentDirRefIsNotEscaped() {
		escapeCase("a\\b", "a%5Cb", "a\\b");
		escapeCase("abc\\def\\gef", "abc%5Cdef%5Cgef", "abc\\def\\gef");
	}

	/**
	 * Some safety agains accidents (build tools are omnipotent so no real
	 * security tried here)
	 */
	@Test
	public void parentDirRefIsEscaped() {
		escapeCase("../b", "..%2Fb");
		escapeCase("a/..", "a%2F..");
		escapeCase("a/../b", "a%2F..%2Fb");
	}

	@Test
	public void startingSlashIsEscaped() {
		escapeCase("/a", "%2Fa");
		escapeCase("//a", "%2F/a", "%2F\\a");
	}

	@Test
	public void startingBackslashIsEscapedButDifferentlyOnWindows() {
		escapeCase("\\a", "%5Ca", "%2Fa");
		escapeCase("\\\\a", "%5C%5Ca", "%2F\\a");
	}

	@Test
	public void twoOrMoreSlashesAreEscaped() {
		escapeCase("a//b", "a/%2Fb", "a\\%2Fb");
		escapeCase("a\\\\b", "a%5C%5Cb", "a\\%2Fb");

		escapeCase("a///b", "a/%2F/b", "a\\%2F\\b");
		escapeCase("a\\\\\\b", "a%5C%5C%5Cb", "a\\%2F\\b");

		escapeCase("a////b", "a/%2F/%2Fb", "a\\%2F\\%2Fb");
		escapeCase("a\\\\\\\\b", "a%5C%5C%5C%5Cb", "a\\%2F\\%2Fb");
	}

	@Test
	public void nontrivialCharsThatAreSafeAndMoreReadableToLeaveUnescaped() {
		escapeCase("a-b", "a-b");
		escapeCase("a?b", "a?b");
	}

	@Test
	public void aRealisticWindowsPathExample() {
		escapeCase("c:\\Documents and Settings\\slave",
				"c%3A%5CDocuments+and+Settings%5Cslave",
				"c%3A\\Documents+and+Settings\\slave");
	}

	@Test
	public void aRealisticWindowsMingwPathExampleWithMixedSeparators() {
		escapeCase("c:\\Documents and Settings\\and/forward/slashes",
				"c%3A%5CDocuments+and+Settings%5Cand/forward/slashes",
				"c%3A\\Documents+and+Settings\\and\\forward\\slashes");
	}

}
