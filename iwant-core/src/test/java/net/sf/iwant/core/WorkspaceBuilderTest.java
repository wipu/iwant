package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class WorkspaceBuilderTest extends TestCase {

	private InputStream originalIn;

	private PrintStream originalOut;

	private PrintStream originalErr;

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private String originalLineSeparator;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	private String testarea;

	private String iwantRoot;

	private String cacheDir;

	public void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
		initializeTestArea();
	}

	private void initializeTestArea() {
		testarea = new File(getClass().getResource("/iwanttestarea").getPath())
				.getAbsolutePath();
		iwantRoot = testarea + "/iwant-as-test";
		ensureEmpty(iwantRoot);
		cacheDir = testarea + "/iwant-cached-test";
		ensureEmpty(cacheDir);
	}

	private static void ensureEmpty(String dirname) {
		File dir = new File(dirname);
		if (!dir.exists())
			dir.mkdir();
		// TODO delete content
	}

	public void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
	}

	public static class EmptyWorkspace {

	}

	public void testTooFewArguments() {
		try {
			WorkspaceBuilder.main(new String[] {
					EmptyWorkspace.class.getName(), iwantRoot,
					"list-of/targets" });
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testListOfTargetsOfEmptyWs() {
		WorkspaceBuilder.main(new String[] { EmptyWorkspace.class.getName(),
				iwantRoot, "list-of/targets", cacheDir });
		assertEquals("", out.toString());
		assertEquals("", err.toString());
	}

	public static class WorkspaceWithConstantTargetFile {

		public Path aConstant() {
			return null;
		}

	}

	public void testListOfTargetsWithOneConstantTargetFile() {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithConstantTargetFile.class.getName(), iwantRoot,
				"list-of/targets", cacheDir });
		assertEquals("aConstant\n", out.toString());
		assertEquals("", err.toString());
	}

	public static class WorkspaceWithTwoConstantTargetFiles {

		public Path constantOne() {
			return null;
		}

		public String notATarget() {
			return null;
		}

		public Path constantTwo() {
			return null;
		}

	}

	public void testListOfTargetsWithTwoConstantTargetFiles() {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), iwantRoot,
				"list-of/targets", cacheDir });
		assertEquals("constantOne\nconstantTwo\n", out.toString());
		assertEquals("", err.toString());
	}

}
