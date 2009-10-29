package net.sf.iwant.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	private String wsRoot;

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
		wsRoot = testarea + "/wsroot";
		ensureEmpty(wsRoot);
		cacheDir = testarea + "/iwant-cached-test";
		ensureEmpty(cacheDir);
	}

	private static void ensureEmpty(String dirname) {
		File dir = new File(dirname);
		del(dir);
		dir.mkdir();
	}

	private static void del(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

	private String cachedContent(String target) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(cacheDir
				+ "/target/" + target));
		StringBuilder actual = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			actual.append(line).append("\n");
		}
		return actual.toString();

	}

	public void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
		System.out.print(out.toString());
		System.err.print(err.toString());
	}

	public static class EmptyWorkspace implements WorkspaceDefinition {

		private static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testTooFewArguments() {
		try {
			WorkspaceBuilder
					.main(new String[] { EmptyWorkspace.class.getName(),
							wsRoot, "list-of/targets" });
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testListOfTargetsOfEmptyWs() {
		WorkspaceBuilder.main(new String[] { EmptyWorkspace.class.getName(),
				wsRoot, "list-of/targets", cacheDir });
		assertEquals("", out.toString());
		assertEquals("", err.toString());
	}

	public static class WorkspaceWithTwoConstantTargetFiles implements
			WorkspaceDefinition {

		private static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Target constantOne() {
				return target("constantOne").content(
						Constant.value("constantOne content\n")).end();
			}

			public Path notATarget() {
				throw new UnsupportedOperationException("Not to be called");
			}

			public String notEvenAPath() {
				throw new UnsupportedOperationException("Not to be called");
			}

			public Target constantTwo() {
				return target("constant2")
						.content(
								Constant
										.value("constantTwo alias constant2 content\n"))
						.end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testListOfTargetsWithTwoConstantTargetFiles() {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"list-of/targets", cacheDir });
		assertEquals("constantOne\nconstantTwo\n", out.toString());
		assertEquals("", err.toString());
	}

	public void testConstantOneAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"target/constantOne/as-path", cacheDir });
		assertTrue(out.toString().endsWith("target/constantOne\n"));
		assertEquals("", err.toString());

		assertEquals("constantOne content\n", cachedContent("constantOne"));
	}

	public void testConstantTwoAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"target/constantTwo/as-path", cacheDir });
		assertTrue(out.toString().endsWith("target/constant2\n"));
		assertEquals("", err.toString());

		assertEquals("constantTwo alias constant2 content\n",
				cachedContent("constant2"));
	}

	public static class WorkspaceWithJavaSrcAndClasses implements
			WorkspaceDefinition {

		private static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source src() {
				return source("src");
			}

			public Target classes() {
				return target("classes").content(
						JavaClasses.compiledFrom(src())).end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testGeneratedJavaClassIsNonempty() throws Exception {
		new File(wsRoot + "/src").mkdir();
		new FileWriter(wsRoot + "/src/Empty.java").append(
				"public class Empty {}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJavaSrcAndClasses.class.getName(), wsRoot,
				"target/classes/as-path", cacheDir });
		assertTrue(out.toString().endsWith("target/classes\n"));
		assertEquals("", err.toString());

		assertTrue(cachedContent("classes/Empty.class").length() > 0);
	}

	public static class WorkspaceWithClassesThatDependOnOtherClasses implements
			WorkspaceDefinition {

		private static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source src1() {
				return source("src1");
			}

			public Target classes1() {
				return target("classes1").content(
						JavaClasses.compiledFrom(src1())).end();
			}

			public Source src2() {
				return source("src2");
			}

			public Target classes2() {
				return target("classes2").content(
						JavaClasses.compiledFrom(src2()).using(classes1()))
						.end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testClassWithDependencyCompiles() throws Exception {
		new File(wsRoot + "/src1").mkdir();
		new FileWriter(wsRoot + "/src1/Util.java").append(
				"public class Util {}\n").close();
		new File(wsRoot + "/src2").mkdir();
		new FileWriter(wsRoot + "/src2/Client.java")
				.append(
						"public class Client {"
								+ " public String foo() {return Util.class.toString();}"
								+ "}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithClassesThatDependOnOtherClasses.class.getName(),
				wsRoot, "target/classes2/as-path", cacheDir });
		assertTrue(out.toString().endsWith("target/classes2\n"));
		assertEquals("", err.toString());

		assertTrue(cachedContent("classes2/Client.class").length() > 0);
		assertTrue(cachedContent("classes1/Util.class").length() > 0);
	}

}
