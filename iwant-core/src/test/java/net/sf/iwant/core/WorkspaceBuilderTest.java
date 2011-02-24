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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

public class WorkspaceBuilderTest extends TestCase {

	private static final boolean SHOW_OUTPUT = false;

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

	private static String mockWeb;

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
		mockWeb = testarea + "/mock-web";
		ensureEmpty(mockWeb);

		String cpitems = testarea + "/iwant/cpitems";
		String eclipseClasses = testarea + "/../../classes";
		if (new File(eclipseClasses).exists()) {
			ensureEmpty(cpitems + "/iwant-core");
			copy(eclipseClasses, cpitems + "/iwant-core");
			copy(testarea + "/../../../iwant-lib-ant-1.7.1/ant-1.7.1.jar",
					cpitems);
			copy(testarea + "/../../../iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar",
					cpitems);
			copy(testarea + "/../../../iwant-lib-junit-3.8.1/junit-3.8.1.jar",
					cpitems);
		}
	}

	private void copy(String from, String to) {
		Copy copy = new Copy();
		File fromFile = new File(from);
		if (fromFile.isDirectory()) {
			Project project = new Project();
			copy.setProject(project);
			FileSet dirSet = new FileSet();
			dirSet.setDir(fromFile);
			dirSet.createPatternSet().setIncludes("**");
			copy.add(dirSet);
		} else {
			copy.setFile(fromFile);
		}
		copy.setTodir(new File(to));
		executeSilently(copy);
	}

	private static void executeSilently(Task task) {
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		ByteArrayOutputStream newErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		System.setErr(new PrintStream(newErr));
		try {
			task.execute();
		} finally {
			System.setOut(oldOut);
			System.setErr(oldErr);
		}
	}

	private static void ensureEmpty(String dirname) {
		File dir = new File(dirname);
		del(dir);
		ensureDir(dir);
	}

	private static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists()) {
			ensureDir(parent);
		}
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
		BufferedReader reader = new BufferedReader(new FileReader(
				pathToCachedTarget(target)));
		StringBuilder actual = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			actual.append(line).append("\n");
		}
		return actual.toString();

	}

	private String pathLine(String target) {
		return pathToCachedTarget(target) + "\n";
	}

	private String pathToCachedTarget(String target) {
		return cacheDir + "/target/" + target;
	}

	/**
	 * TODO we really need mocking
	 */
	private void sleep() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
		if (SHOW_OUTPUT) {
			System.out.print(out.toString());
			System.err.print(err.toString());
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

	public void testListOfTargetsWithTwoConstantTargetFiles() {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"list-of/targets", cacheDir });
		assertEquals("constant2-container/constant2\nconstantOne\n",
				out.toString());
		assertEquals("", err.toString());
	}

	public void testIllegalTargetAsPath() {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithTwoConstantTargetFiles.class.getName(),
					wsRoot, "target/illegal/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out.toString());
		assertEquals("", err.toString());
	}

	public void testConstantOneAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"target/constantOne/as-path", cacheDir });
		assertEquals(pathLine("constantOne"), out.toString());
		assertEquals("", err.toString());

		assertEquals("constantOne content\n", cachedContent("constantOne"));
	}

	public void testConstantTwoAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot,
				"target/constant2-container/constant2/as-path", cacheDir });
		assertEquals(pathLine("constant2-container/constant2"), out.toString());
		assertEquals("", err.toString());

		assertEquals("constantTwo alias constant2 content\n",
				cachedContent("constant2-container/constant2"));
	}

	public static class WorkspaceWithJavaSrcAndClasses implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source src() {
				return source("src");
			}

			public Target<JavaClasses> classes() {
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
		assertEquals(pathLine("classes"), out.toString());
		assertEquals("", err.toString());

		assertTrue(cachedContent("classes/Empty.class").length() > 0);
	}

	public static class WorkspaceWithClassesThatDependOnOtherClasses implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source src1() {
				return source("src1");
			}

			public Target<JavaClasses> classes1() {
				return target("classes1").content(
						JavaClasses.compiledFrom(src1())).end();
			}

			public Source src2() {
				return source("src2");
			}

			public Target<JavaClasses> classes2() {
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
				.append("public class Client {"
						+ " public String foo() {return Util.class.toString();}"
						+ "}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithClassesThatDependOnOtherClasses.class.getName(),
				wsRoot, "target/classes2/as-path", cacheDir });
		assertEquals(pathLine("classes2"), out.toString());
		assertEquals("", err.toString());

		assertTrue(cachedContent("classes2/Client.class").length() > 0);
		assertTrue(cachedContent("classes1/Util.class").length() > 0);
	}

	public static class WorkspaceWithJunitTests implements WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source src() {
				return source("src");
			}

			public Target<JavaClasses> classes() {
				return target("classes").content(
						JavaClasses.compiledFrom(src())).end();
			}

			public Source tests() {
				return source("tests");
			}

			public Target<JavaClasses> testClasses() {
				return target("testClasses").content(
						JavaClasses.compiledFrom(tests()).using(classes())
								.using(builtin().junit381Classes())).end();
			}

			public Target<JunitResult> testResult() {
				return target("testResult")
						.content(
								JunitResult
										.ofClass("ATest")
										.using(testClasses())
										.using(testClasses().content()
												.classpathItems())).end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testJunitResultOfFailingTest() throws Exception {
		new File(wsRoot + "/tests").mkdir();
		new FileWriter(wsRoot + "/tests/ATest.java").append(
				"public class ATest extends junit.framework.TestCase {"
						+ " public void testValue() {"
						+ "  assertEquals(1, AProd.value());}}\n").close();
		new File(wsRoot + "/src").mkdir();
		new FileWriter(wsRoot + "/src/AProd.java").append(
				"public class AProd {"
						+ " public static int value() {return 2;}}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot,
				"target/testResult/as-path", cacheDir });
		assertEquals(pathLine("testResult"), out.toString());
		assertTrue(err.toString().contains("ATest FAILED"));
	}

	public void testJunitResultOfPassingTest() throws Exception {
		new File(wsRoot + "/tests").mkdir();
		new FileWriter(wsRoot + "/tests/ATest.java").append(
				"public class ATest extends junit.framework.TestCase {"
						+ " public void testValue() {"
						+ "  assertEquals(1, AProd.value());}}\n").close();
		new File(wsRoot + "/src").mkdir();
		new FileWriter(wsRoot + "/src/AProd.java").append(
				"public class AProd {"
						+ " public static int value() {return 1;}}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot,
				"target/testResult/as-path", cacheDir });
		assertEquals(pathLine("testResult"), out.toString());
		assertEquals("", err.toString());
	}

	/**
	 * Let's test laziness when sources are directories like for javac
	 */
	public void testJunitResultIsFailureEvenIfSourcesAreTouchedAfterSuccess()
			throws Exception {
		testJunitResultOfPassingTest();
		sleep();
		new FileWriter(wsRoot + "/src/AProd.java", false).append(
				"public class AProd {"
						+ " public static int value() {return 2;}}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot,
				"target/testResult/as-path", cacheDir });
		assertTrue(err.toString().contains("ATest FAILED"));
	}

	public static class WorkspaceWithDownloadedContent implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Target<Downloaded> aDownloadedFile() {
				return target("aDownloadedFile").content(
						Downloaded.from("file://" + mockWeb + "/aFileInTheWeb")
								.md5("971ff50db55ffc43bdf06674fc81c885")).end();
			}

			public Target<Downloaded> aDownloadedFileWithSha() {
				return target("aDownloadedFileWithSha")
						.content(
								Downloaded
										.from("file://" + mockWeb
												+ "/aFileInTheWeb")
										.sha("97d539c5f1c4a59bd9ef0d1ec6df35ecda052020"))
						.end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testDownloadFailsIfFileDoesNotExist() throws Exception {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot,
					"target/aDownloadedFile/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out.toString());
		assertTrue(err.toString().contains("Error getting"));
	}

	public void testDownloadFailsIfDownloadedFileIsCorrupt() throws Exception {
		new FileWriter(mockWeb + "/aFileInTheWeb").append("corrupted\n")
				.close();
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot,
					"target/aDownloadedFile/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out.toString());
		assertTrue(err.toString().contains("Checksum failed"));
	}

	public void testDownloadFailsIfShaDoesNotMatch() throws Exception {
		new FileWriter(mockWeb + "/aFileInTheWeb").append("corrupted\n")
				.close();
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot,
					"target/aDownloadedFileWithSha/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out.toString());
		assertTrue(err.toString().contains("Checksum failed"));
	}

	public void testDownloadRetryWorksAfterFailedDownload() throws Exception {
		testDownloadFailsIfDownloadedFileIsCorrupt();
		new FileWriter(mockWeb + "/aFileInTheWeb").append("correct\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot,
				"target/aDownloadedFile/as-path", cacheDir });
		assertEquals(pathLine("aDownloadedFile"), out.toString());
		assertTrue(err.toString().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFile"));
	}

	/**
	 * Custom cache invalidation logic needed to negate this feature, if needed
	 * for the paranoid among us
	 */
	public void testDownloadDoesNotFailIfAlreadyCachedFileIsCorrupt()
			throws Exception {
		testSuccessfulFirstDownload();
		sleep();
		new FileWriter(pathToCachedTarget("aDownloadedFile")).append(
				"corrupted\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot,
				"target/aDownloadedFile/as-path", cacheDir });
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out.toString());
		assertEquals("corrupted\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownload() throws Exception {
		new FileWriter(mockWeb + "/aFileInTheWeb").append("correct\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot,
				"target/aDownloadedFile/as-path", cacheDir });
		assertEquals(pathLine("aDownloadedFile"), out.toString());
		assertTrue(err.toString().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownloadWithSha() throws Exception {
		new FileWriter(mockWeb + "/aFileInTheWeb").append("correct\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot,
				"target/aDownloadedFileWithSha/as-path", cacheDir });
		assertEquals(pathLine("aDownloadedFileWithSha"), out.toString());
		assertTrue(err.toString().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFileWithSha"));
	}

	public void testSuccessfulLazyDownloadWhenCorrectCachedFileExists()
			throws Exception {
		testSuccessfulFirstDownload();

		long cachedFileModifiedAt = new File(
				pathToCachedTarget("aDownloadedFile")).lastModified();
		sleep();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot,
				"target/aDownloadedFile/as-path", cacheDir });
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out.toString());
		assertEquals("Getting: file:" + mockWeb + "/aFileInTheWeb\n" + "To: "
				+ pathToCachedTarget("aDownloadedFile") + "\n", err.toString());
		assertEquals("correct\n", cachedContent("aDownloadedFile"));
		assertEquals(cachedFileModifiedAt, new File(
				pathToCachedTarget("aDownloadedFile")).lastModified());
	}

	public static class WorkspaceWithEclipseProjects implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Target<EclipseProjects> eclipseProjects() {
				return target("eclipse-projects").content(
						EclipseProjects.with().project(aEclipseProject())
								.project(bEclipseProject())).end();
			}

			private EclipseProject aEclipseProject() {
				return EclipseProject.with().name("a").src("src/main/java")
						.libs(aClasses().dependencies()).end();
			}

			public Source aSrc() {
				return source("a/src");
			}

			public Target<JavaClasses> aClasses() {
				return target("a-classes").content(
						JavaClasses.compiledFrom(aSrc()).using(bClasses()))
						.end();
			}

			private EclipseProject bEclipseProject() {
				JavaClasses bClassesContent = bClasses().content();
				return EclipseProject.with().name("b").src("src").src("tests")
						.iwantAnt(aClasses(), bClasses())
						.libs(bClassesContent.classpathItems()).end();
			}

			public Source bSrc() {
				return source("b/src");
			}

			public Target<JavaClasses> bClasses() {
				return target("b-classes").content(
						JavaClasses.compiledFrom(bSrc()).using(
								builtin().junit381Classes())).end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testEclipseProjectsFailsIfCompilationFails() throws IOException {
		ensureEmpty(wsRoot + "/a/src");
		ensureEmpty(wsRoot + "/b/src");
		new FileWriter(wsRoot + "/b/src/B.java").append(
				"public class B {compilationFailure}\n").close();
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithEclipseProjects.class.getName(), wsRoot,
					"target/eclipse-projects/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
		}
	}

	public void testEclipseProjectsWithTwoMinimalProjectsWithDependency()
			throws IOException {
		ensureEmpty(wsRoot + "/a/src");
		ensureEmpty(wsRoot + "/b/src");
		ensureEmpty(wsRoot + "/b/tests");
		new FileWriter(wsRoot + "/a/src/A.java").append(
				"public class A { public B b;}\n").close();
		new FileWriter(wsRoot + "/b/src/B.java").append("public class B { }\n")
				.close();
		new FileWriter(wsRoot + "/b/tests/BTest.java").append(
				"public class BTest extends org.junit.TestCase { }\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithEclipseProjects.class.getName(), wsRoot,
				"target/eclipse-projects/as-path", cacheDir });
		assertEquals(pathLine("eclipse-projects"), out.toString());
		assertEquals("", err.toString());

		StringBuilder ap = new StringBuilder();
		ap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		ap.append("<projectDescription>\n");
		ap.append("        <name>a</name>\n");
		ap.append("        <comment></comment>\n");
		ap.append("        <projects>\n");
		ap.append("        </projects>\n");
		ap.append("        <buildSpec>\n");
		ap.append("                <buildCommand>\n");
		ap.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		ap.append("                        <arguments>\n");
		ap.append("                        </arguments>\n");
		ap.append("                </buildCommand>\n");
		ap.append("        </buildSpec>\n");
		ap.append("        <natures>\n");
		ap.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		ap.append("        </natures>\n");
		ap.append("</projectDescription>\n");
		assertEquals(ap.toString(),
				cachedContent("eclipse-projects/a/.project"));
		StringBuilder ac = new StringBuilder();
		ac.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		ac.append("<classpath>\n");
		ac.append("        <classpathentry kind=\"src\" path=\"src/main/java\"/>\n");
		ac.append("        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		ac.append("        <classpathentry kind=\"lib\" path=\"" + cacheDir
				+ "/target/b-classes\"/>\n");
		ac.append("        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		ac.append("</classpath>\n");
		assertEquals(ac.toString(),
				cachedContent("eclipse-projects/a/.classpath"));

		StringBuilder bp = new StringBuilder();
		bp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bp.append("<projectDescription>\n");
		bp.append("        <name>b</name>\n");
		bp.append("        <comment></comment>\n");
		bp.append("        <projects>\n");
		bp.append("        </projects>\n");
		bp.append("        <buildSpec>\n");
		bp.append("                <buildCommand>\n");
		bp.append("                        <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
		bp.append("                        <triggers>auto,full,incremental,</triggers>\n");
		bp.append("                        <arguments>\n");
		bp.append("                                <dictionary>\n");
		bp.append("                                        <key>LaunchConfigHandle</key>\n");
		bp.append("                                        <value>&lt;project&gt;/.externalToolBuilders/iwant-ant-for-eclipse.launch</value>\n");
		bp.append("                                </dictionary>\n");
		bp.append("                        </arguments>\n");
		bp.append("                </buildCommand>\n");
		bp.append("                <buildCommand>\n");
		bp.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		bp.append("                        <arguments>\n");
		bp.append("                        </arguments>\n");
		bp.append("                </buildCommand>\n");
		bp.append("        </buildSpec>\n");
		bp.append("        <natures>\n");
		bp.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		bp.append("        </natures>\n");
		bp.append("</projectDescription>\n");
		assertEquals(bp.toString(),
				cachedContent("eclipse-projects/b/.project"));

		StringBuilder bc = new StringBuilder();
		bc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bc.append("<classpath>\n");
		bc.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		bc.append("        <classpathentry kind=\"src\" path=\"tests\"/>\n");
		bc.append("        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		bc.append("        <classpathentry kind=\"lib\" path=\"" + testarea
				+ "/iwant/cpitems/junit-3.8.1.jar\"/>\n");
		bc.append("        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		bc.append("</classpath>\n");
		assertEquals(bc.toString(),
				cachedContent("eclipse-projects/b/.classpath"));

		StringBuilder bl = new StringBuilder();
		bl.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		bl.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
		bl.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_AFTER_CLEAN_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_AUTO_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_MANUAL_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		bl.append("<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
		bl.append("<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/a&quot; type=&quot;4&quot;/&gt;&#10;&lt;item path=&quot;/b&quot; type=&quot;4&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		bl.append("<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		bl.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"b\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/b/src&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/b/build.xml}\"/>\n");
		bl.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,incremental,auto,\"/>\n");
		bl.append("<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
		bl.append("<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		bl.append("</launchConfiguration>\n");
		assertEquals(
				bl.toString(),
				cachedContent("eclipse-projects/b/.externalToolBuilders/iwant-ant-for-eclipse.launch"));

		StringBuilder bx = new StringBuilder();
		bx.append("<project name=\"b-iwant\" default=\"list-of-targets\" basedir=\".\">\n");
		bx.append("\n");
		bx.append("	<property name=\"i-have\" location=\"i-have\" />\n");
		bx.append("	<property file=\"${i-have}/ws-info.conf\" prefix=\"ws-info\" />\n");
		bx.append("	<property name=\"ws-name\" value=\"${ws-info.WSNAME}\" />\n");
		bx.append("	<property name=\"ws-root\" location=\"${i-have}/${ws-info.WSROOT}\" />\n");
		bx.append("	<property name=\"wsdef-src\" location=\"${i-have}/${ws-info.WSDEF_SRC}\" />\n");
		bx.append("	<property name=\"wsdef-classname\" value=\"${ws-info.WSDEF_CLASS}\" />\n");
		bx.append("\n");
		bx.append("	<target name=\"wishdir\">\n");
		bx.append("		<property name=\"wishdir\" location=\"iwant\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"cached\" depends=\"wishdir\">\n");
		bx.append("		<property name=\"cached\" location=\"${wishdir}/cached\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"my-cached\" depends=\"cached\">\n");
		bx.append("		<property name=\"my-cached\" location=\"${cached}/build-xml\" />\n");
		bx.append("		<mkdir dir=\"${my-cached}\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"iwant-classpath\" depends=\"cached\">\n");
		bx.append("		<path id=\"iwant-classpath\">\n");
		bx.append("			<pathelement location=\"${cached}/iwant/cpitems/iwant-core\" />\n");
		bx.append("			<fileset dir=\"${cached}/iwant/cpitems\">\n");
		bx.append("				<include name=\"*.jar\" />\n");
		bx.append("			</fileset>\n");
		bx.append("		</path>\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"wsdef-classes\" depends=\"iwant-classpath, my-cached\">\n");
		bx.append("		<property name=\"wsdef-classes\" location=\"${my-cached}/wsdef-classes\" />\n");
		bx.append("		<mkdir dir=\"${wsdef-classes}\" />\n");
		bx.append("		<javac destdir=\"${wsdef-classes}\" srcdir=\"${wsdef-src}\" classpathref=\"iwant-classpath\">\n");
		bx.append("		</javac>\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<macrodef name=\"iwant\">\n");
		bx.append("		<attribute name=\"target-name\" />\n");
		bx.append("		<sequential>\n");
		bx.append("			<java dir=\"${ws-root}\" classname=\"net.sf.iwant.core.WorkspaceBuilder\" fork=\"true\" outputproperty=\"iwant-out\" resultproperty=\"iwant-result\">\n");
		bx.append("				<arg value=\"${wsdef-classname}\" />\n");
		bx.append("				<arg value=\"${ws-root}\" />\n");
		bx.append("				<arg value=\"@{target-name}\" />\n");
		bx.append("				<arg value=\"${cached}/${ws-name}\" />\n");
		bx.append("				<classpath>\n");
		bx.append("					<path refid=\"iwant-classpath\" />\n");
		bx.append("					<path location=\"${wsdef-classes}\" />\n");
		bx.append("				</classpath>\n");
		bx.append("			</java>\n");
		bx.append("			<echo message=\"${iwant-out}\" />\n");
		bx.append("			<condition property=\"iwant-succeeded\">\n");
		bx.append("				<equals arg1=\"0\" arg2=\"${iwant-result}\" />\n");
		bx.append("			</condition>\n");
		bx.append("			<fail message=\"Failure\" unless=\"iwant-succeeded\" />\n");
		bx.append("		</sequential>\n");
		bx.append("	</macrodef>\n");
		bx.append("\n");
		bx.append("	<target name=\"list-of-targets\" depends=\"wsdef-classes\">\n");
		bx.append("		<iwant target-name=\"list-of/targets\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"fresh-eclipse-settings\" depends=\"wsdef-classes\">\n");
		bx.append("		<iwant target-name=\"target/eclipse-projects/as-path\" />\n");
		bx.append("		<copy todir=\"${ws-root}\">\n");
		bx.append("			<fileset dir=\"${iwant-out}\" includes=\"**/*\" />\n");
		bx.append("		</copy>\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"a-classes-as-path\" depends=\"wsdef-classes\" description=\"target/a-classes/as-path\">\n");
		bx.append("		<iwant target-name=\"target/a-classes/as-path\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("	<target name=\"b-classes-as-path\" depends=\"wsdef-classes\" description=\"target/b-classes/as-path\">\n");
		bx.append("		<iwant target-name=\"target/b-classes/as-path\" />\n");
		bx.append("	</target>\n");
		bx.append("\n");
		bx.append("</project>\n");
		bx.append("");
		assertEquals(bx.toString(),
				cachedContent("eclipse-projects/b/build.xml"));
	}

	public static class WorkspaceWithReferenceToNextPhase implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Source phase2Src() {
				return source("phase2/src");
			}

			public Target<JavaClasses> phase2Classes() {
				return target("phase2Classes").content(
						JavaClasses.compiledFrom(phase2Src()).using(
								builtin().all())).end();
			}

			public NextPhase phaseTwo() {
				return NextPhase.at(phase2Classes()).named(
						"com.example.phasetwo.Phase2");
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	private static String phase2java() {
		StringBuilder b = new StringBuilder();
		b.append("package com.example.phasetwo;\n");
		b.append("\n");
		b.append("import net.sf.iwant.core.Constant;\n");
		b.append("import net.sf.iwant.core.ContainerPath;\n");
		b.append("import net.sf.iwant.core.Locations;\n");
		b.append("import net.sf.iwant.core.RootPath;\n");
		b.append("import net.sf.iwant.core.Target;\n");
		b.append("import net.sf.iwant.core.WorkspaceDefinition;\n");
		b.append("\n");
		b.append("public class Phase2 implements WorkspaceDefinition {\n");
		b.append("		public static class Root extends RootPath {\n");
		b.append("\n");
		b.append("			public Root(Locations locations) {\n");
		b.append("				super(locations);\n");
		b.append("			}\n");
		b.append("\n");
		b.append("			public Target targetInPhase2() {\n");
		b.append("				return target(\"targetInPhase2\").content(\n");
		b.append("						Constant.value(\"hello from phase2\")).end();\n");
		b.append("			}\n");
		b.append("\n");
		b.append("		}\n");
		b.append("\n");
		b.append("		public ContainerPath wsRoot(Locations locations) {\n");
		b.append("			return new Root(locations);\n");
		b.append("		}\n");
		b.append("}\n");
		return b.toString();
	}

	public void testListOfTargetsContainsPhase2Target() throws IOException {
		ensureEmpty(wsRoot + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot + "/phase2/src/com/example/phasetwo/Phase2.java")
				.append(phase2java()).close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithReferenceToNextPhase.class.getName(), wsRoot,
				"list-of/targets", cacheDir });
		assertEquals("", err.toString());
		assertEquals("phase2Classes\ntargetInPhase2\n", out.toString());
	}

	public void testPhase2TargetContent() throws IOException {
		ensureEmpty(wsRoot + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot + "/phase2/src/com/example/phasetwo/Phase2.java")
				.append(phase2java()).close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithReferenceToNextPhase.class.getName(), wsRoot,
				"target/targetInPhase2/as-path", cacheDir });
		assertEquals("", err.toString());
		assertEquals(pathLine("targetInPhase2"), out.toString());
		assertEquals("hello from phase2\n", cachedContent("targetInPhase2"));
	}

	public static class WorkspaceWithShellScript implements WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Target<Constant> successfulScript() {
				StringBuilder b = new StringBuilder();
				b.append("#!/bin/bash\n");
				b.append("set -eu\n");
				b.append("DEST=$1\n");
				b.append("echo this was printed to stderr > /dev/stderr\n");
				b.append("echo this was printed to stdout\n");
				b.append("echo DEST=$DEST\n");
				b.append("echo -n 'pwd is '\n");
				b.append("pwd\n");
				b.append("echo 'hello from script' > \"$DEST\"\n");
				return target("successfulScript").content(
						Constant.value(b.toString())).end();
			}

			public Target<ScriptGeneratedContent> successfulScriptOutput() {
				return target("successfulScriptOutput").content(
						ScriptGeneratedContent.of(successfulScript())).end();
			}

			public Target<Constant> failingScript() {
				StringBuilder b = new StringBuilder();
				b.append("#!/bin/bash\n");
				b.append("echo causing failure\n");
				b.append("exit 1\n");
				return target("failingScript").content(
						Constant.value(b.toString())).end();
			}

			public Target<ScriptGeneratedContent> failingScriptOutput() {
				return target("failingScriptOutput").content(
						ScriptGeneratedContent.of(failingScript())).end();
			}

		}

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testSuccessfulShellScript() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithShellScript.class.getName(), wsRoot,
				"target/successfulScriptOutput/as-path", cacheDir });

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("Standard out:\n");
		expectedErr.append("this was printed to stdout\n");
		expectedErr.append("DEST="
				+ pathToCachedTarget("successfulScriptOutput") + "\n");
		expectedErr.append("pwd is " + cacheDir
				+ "/tmp-for-the-only-worker-thread\n");
		expectedErr.append("Standard err:\n");
		expectedErr.append("this was printed to stderr\n");

		assertEquals(pathLine("successfulScriptOutput"), out.toString());
		assertEquals(expectedErr.toString(), err.toString());

		assertEquals("hello from script\n",
				cachedContent("successfulScriptOutput"));
	}

	public void testFailingShellScript() {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithShellScript.class.getName(), wsRoot,
					"target/failingScriptOutput/as-path", cacheDir });
			fail();
		} catch (Exception e) {
			// expected
			assertEquals(
					"java.lang.IllegalStateException: Script exited with non-zero status 1",
					e.getMessage());
		}

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("Standard out:\n");
		expectedErr.append("causing failure\n");
		expectedErr.append("Standard err:\n");

		assertEquals("", out.toString());
		assertEquals(expectedErr.toString(), err.toString());

		assertFalse(new File(pathToCachedTarget("failingScriptOutput"))
				.exists());
	}

}
