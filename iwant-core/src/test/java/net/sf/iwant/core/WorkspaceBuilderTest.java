package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.core.Concatenated.ConcatenatedBuilder;

public class WorkspaceBuilderTest extends WorkspaceBuilderTestBase {

	public void testListOfTargetsOfEmptyWs() {
		at(EmptyWorkspace.class).iwant("list-of/targets");
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testListOfTargetsWithTwoConstantTargetFiles() {
		at(WorkspaceWithTwoConstantTargetFiles.class).iwant("list-of/targets");
		assertEquals("pout:constant2-container/constant2\npout:constantOne\n",
				out());
		assertEquals("", err());
	}

	public void testIllegalTargetAsPath() {
		try {
			at(WorkspaceWithTwoConstantTargetFiles.class).iwant(
					"target/illegal/as-path");
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testConstantOneAsPathAndItsContent() throws IOException {
		at(WorkspaceWithTwoConstantTargetFiles.class).iwant(
				"target/constantOne/as-path");
		assertEquals(pathLine("constantOne"), out());
		assertEquals("", err());

		assertEquals("constantOne content\n", cachedContent("constantOne"));
	}

	public void testConstantTwoAsPathAndItsContent() throws IOException {
		at(WorkspaceWithTwoConstantTargetFiles.class).iwant(
				"target/constant2-container/constant2/as-path");
		assertEquals(pathLine("constant2-container/constant2"), out());
		assertEquals("", err());

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

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testGeneratedJavaClassIsNonempty() throws Exception {
		directoryExists("src");
		file("src/Empty.java").withContent().line("public class Empty {}")
				.exists();

		at(WorkspaceWithJavaSrcAndClasses.class)
				.iwant("target/classes/as-path");
		assertEquals(pathLine("classes"), out());
		assertEquals("", err());

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

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testClassWithDependencyCompiles() throws Exception {
		directoryExists("src1");
		file("src1/Util.java").withContent().line("public class Util {}")
				.exists();

		directoryExists("src2");
		file("src2/Client.java").withContent();
		line("public class Client {");
		line("  public String foo() {return Util.class.toString();}");
		line("}");
		exists();

		at(WorkspaceWithClassesThatDependOnOtherClasses.class).iwant(
				"target/classes2/as-path");
		assertEquals(pathLine("classes2"), out());
		assertEquals("", err());

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

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testJunitResultOfFailingTest() throws Exception {
		directoryExists("tests");
		file("tests/ATest.java").withContent();
		line("public class ATest extends junit.framework.TestCase {");
		line("  public void testValue() {");
		line("    assertEquals(1, AProd.value());}}");
		exists();
		directoryExists("src");
		file("src/AProd.java").withContent();
		line("public class AProd {");
		line("  public static int value() {return 2;}}");
		exists();

		at(WorkspaceWithJunitTests.class).iwant("target/testResult/as-path");
		assertEquals(pathLine("testResult"), out());
		assertEquals("perr:Test ATest FAILED\n", err());
	}

	public void testJunitResultOfPassingTest() throws Exception {
		new File(wsRoot() + "/tests").mkdir();
		new FileWriter(wsRoot() + "/tests/ATest.java").append(
				"public class ATest extends junit.framework.TestCase {"
						+ " public void testValue() {"
						+ "  assertEquals(1, AProd.value());}}\n").close();
		new File(wsRoot() + "/src").mkdir();
		new FileWriter(wsRoot() + "/src/AProd.java").append(
				"public class AProd {"
						+ " public static int value() {return 1;}}\n").close();

		at(WorkspaceWithJunitTests.class).iwant("target/testResult/as-path");
		assertEquals(pathLine("testResult"), out());
		assertEquals("", err());
	}

	/**
	 * Let's test laziness when sources are directories like for javac
	 */
	public void testJunitResultIsFailureEvenIfSourcesAreTouchedAfterSuccess()
			throws Exception {
		testJunitResultOfPassingTest();
		sleep();
		new FileWriter(wsRoot() + "/src/AProd.java", false).append(
				"public class AProd {"
						+ " public static int value() {return 2;}}\n").close();

		at(WorkspaceWithJunitTests.class).iwant("target/testResult/as-path");
		assertTrue(err().contains("ATest FAILED"));
	}

	public static class WorkspaceWithDownloadedContent implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			public Target<Downloaded> aDownloadedFile() {
				return target("aDownloadedFile").content(
						Downloaded.from(
								"file://" + mockWeb() + "/aFileInTheWeb").md5(
								"971ff50db55ffc43bdf06674fc81c885")).end();
			}

			public Target<Downloaded> aDownloadedFileWithSha() {
				return target("aDownloadedFileWithSha").content(
						Downloaded.from(
								"file://" + mockWeb() + "/aFileInTheWeb").sha(
								"97d539c5f1c4a59bd9ef0d1ec6df35ecda052020"))
						.end();
			}

		}

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testDownloadFailsIfFileDoesNotExist() throws Exception {
		try {
			at(WorkspaceWithDownloadedContent.class).iwant(
					"target/aDownloadedFile/as-path");
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out());
		assertTrue(err().contains("Error getting"));
	}

	public void testDownloadFailsIfDownloadedFileIsCorrupt() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("corrupted\n")
				.close();
		try {
			at(WorkspaceWithDownloadedContent.class).iwant(
					"target/aDownloadedFile/as-path");
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out());
		assertTrue(err().contains("Checksum failed"));
	}

	public void testDownloadFailsIfShaDoesNotMatch() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("corrupted\n")
				.close();
		try {
			at(WorkspaceWithDownloadedContent.class).iwant(
					"target/aDownloadedFileWithSha/as-path");
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out());
		assertTrue(err().contains("Checksum failed"));
	}

	public void testDownloadRetryWorksAfterFailedDownload() throws Exception {
		testDownloadFailsIfDownloadedFileIsCorrupt();
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("correct\n")
				.close();
		at(WorkspaceWithDownloadedContent.class).iwant(
				"target/aDownloadedFile/as-path");
		assertEquals(pathLine("aDownloadedFile"), out());
		assertTrue(err().contains("Getting"));
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
		at(WorkspaceWithDownloadedContent.class).iwant(
				"target/aDownloadedFile/as-path");
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out());
		assertEquals("corrupted\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownload() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("correct\n")
				.close();
		at(WorkspaceWithDownloadedContent.class).iwant(
				"target/aDownloadedFile/as-path");
		assertEquals(pathLine("aDownloadedFile"), out());
		assertTrue(err().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownloadWithSha() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("correct\n")
				.close();
		at(WorkspaceWithDownloadedContent.class).iwant(
				"target/aDownloadedFileWithSha/as-path");
		assertEquals(pathLine("aDownloadedFileWithSha"), out());
		assertTrue(err().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFileWithSha"));
	}

	public void testSuccessfulLazyDownloadWhenCorrectCachedFileExists()
			throws Exception {
		testSuccessfulFirstDownload();

		long cachedFileModifiedAt = new File(
				pathToCachedTarget("aDownloadedFile")).lastModified();
		sleep();
		at(WorkspaceWithDownloadedContent.class).iwant(
				"target/aDownloadedFile/as-path");
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out());
		assertEquals("perr:Downloading file://" + mockWeb()
				+ "/aFileInTheWeb\nGetting: file:" + mockWeb()
				+ "/aFileInTheWeb\n" + "To: "
				+ pathToCachedTarget("aDownloadedFile") + "\n", err());
		assertEquals("correct\n", cachedContent("aDownloadedFile"));

		// TODO find a way to keep the sleep hack invisible to test
		assertEquals(cachedFileModifiedAt - 2000L, new File(
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

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testEclipseProjectsFailsIfCompilationFails() throws IOException {
		ensureEmpty(wsRoot() + "/a/src");
		ensureEmpty(wsRoot() + "/b/src");
		new FileWriter(wsRoot() + "/b/src/B.java").append(
				"public class B {compilationFailure}\n").close();
		try {
			at(WorkspaceWithEclipseProjects.class).iwant(
					"target/eclipse-projects/as-path");
			fail();
		} catch (Exception e) {
			// expected
		}
	}

	public void testEclipseProjectsWithTwoMinimalProjectsWithDependency()
			throws IOException {
		ensureEmpty(wsRoot() + "/a/src");
		ensureEmpty(wsRoot() + "/b/src");
		ensureEmpty(wsRoot() + "/b/tests");
		new FileWriter(wsRoot() + "/a/src/A.java").append(
				"public class A { public B b;}\n").close();
		new FileWriter(wsRoot() + "/b/src/B.java").append(
				"public class B { }\n").close();
		new FileWriter(wsRoot() + "/b/tests/BTest.java").append(
				"public class BTest extends org.junit.TestCase { }\n").close();
		at(WorkspaceWithEclipseProjects.class).iwant(
				"target/eclipse-projects/as-path");
		assertEquals(pathLine("eclipse-projects"), out());
		assertEquals("", err());

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
		ac.append("        <classpathentry kind=\"lib\" path=\"" + cacheDir()
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
		bc.append("        <classpathentry kind=\"lib\" path=\""
				+ testarea()
				+ "/.internal/iwant/iwant-bootstrapper/phase2/iw/cached/.internal/bin/junit-3.8.1.jar\"/>\n");
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

		@Override
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
		ensureEmpty(wsRoot() + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot()
				+ "/phase2/src/com/example/phasetwo/Phase2.java").append(
				phase2java()).close();
		at(WorkspaceWithReferenceToNextPhase.class).iwant("list-of/targets");
		assertEquals("", err());
		assertEquals("pout:phase2Classes\npout:targetInPhase2\n", out());
	}

	public void testPhase2TargetContent() throws IOException {
		ensureEmpty(wsRoot() + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot()
				+ "/phase2/src/com/example/phasetwo/Phase2.java").append(
				phase2java()).close();
		at(WorkspaceWithReferenceToNextPhase.class).iwant(
				"target/targetInPhase2/as-path");
		assertEquals("", err());
		assertEquals(pathLine("targetInPhase2"), out());
		assertEquals("hello from phase2", cachedContent("targetInPhase2"));
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

			public Target<Constant> anotherSuccessfulScript() {
				StringBuilder b = new StringBuilder();
				b.append("#!/bin/bash\n");
				b.append("set -eu\n");
				b.append("DEST=$1\n");
				b.append("echo 'hello from anotherSuccessfulScript' > \"$DEST\"\n");
				return target("anotherSuccessfulScript").content(
						Constant.value(b.toString())).end();
			}

			public Target<ScriptGeneratedContent> anotherSuccessfulScriptOutput() {
				return target("anotherSuccessfulScriptOutput").content(
						ScriptGeneratedContent.of(anotherSuccessfulScript()))
						.end();
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

			public Source mkdirScriptSource() {
				return source("mkdirScriptSource");
			}

			public Target<Concatenated> mkdirScript() {
				ConcatenatedBuilder b = Concatenated.from();
				b.string("#!/bin/bash\n");
				b.string("set -eu\n");
				b.string("DEST=$1\n");
				b.string("mkdir \"$DEST\"\n");
				b.string("cp '").pathTo(mkdirScriptSource())
						.string("' \"$DEST\"/\n");
				return target("mkdirScript").content(b.end()).end();
			}

			public Target<ScriptGeneratedContent> mkdirScriptGeneratedContent() {
				return target("mkdirScriptGeneratedContent").content(
						ScriptGeneratedContent.of(mkdirScript())).end();
			}

		}

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testSuccessfulShellScript() throws IOException {
		at(WorkspaceWithShellScript.class).iwant(
				"target/successfulScriptOutput/as-path");

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("perr:this was printed to stderr\n");
		expectedErr.append("perr:this was printed to stdout\n");
		expectedErr.append("perr:DEST="
				+ pathToCachedTarget("successfulScriptOutput") + "\n");
		expectedErr.append("perr:pwd is " + cacheDir()
				+ "/tmp-for-the-only-worker-thread\n");

		assertEquals(pathLine("successfulScriptOutput"), out());
		assertEquals(expectedErr.toString(), err());

		assertEquals("hello from script\n",
				cachedContent("successfulScriptOutput"));
	}

	/**
	 * A bug made the second run run the first script again.
	 */
	public void testAnotherScriptAfterOneScript() throws IOException {
		testSuccessfulShellScript();
		startOfOutAndErrCapture();
		at(WorkspaceWithShellScript.class).iwant(
				"target/anotherSuccessfulScriptOutput/as-path");

		assertEquals(pathLine("anotherSuccessfulScriptOutput"), out());
		assertEquals("", err());

		assertEquals("hello from anotherSuccessfulScript\n",
				cachedContent("anotherSuccessfulScriptOutput"));
	}

	public void testFailingShellScript() {
		try {
			at(WorkspaceWithShellScript.class).iwant(
					"target/failingScriptOutput/as-path");
			fail();
		} catch (Exception e) {
			// expected
			assertEquals(
					"java.lang.IllegalStateException: Script exited with non-zero status 1",
					e.getMessage());
		}

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("perr:causing failure\n");

		assertEquals("", out());
		assertEquals(expectedErr.toString(), err());

		assertFalse(new File(pathToCachedTarget("failingScriptOutput"))
				.exists());
	}

	public void testConcatenatedSrc() throws IOException {
		new FileWriter(wsRoot() + "/src").append("src content\n").close();
		at(WorkspaceWithConcatenatedContent.class).iwant(
				"target/copyOfSrc/as-path");

		assertEquals(pathLine("copyOfSrc"), out());
		assertEquals("", err());

		assertEquals("src content\n", cachedContent("copyOfSrc"));
	}

	public void testConcatenatedSrcAfterSrcChanges() throws IOException {
		testConcatenatedSrc();
		sleep();
		new FileWriter(wsRoot() + "/src").append("new src content\n").close();
		at(WorkspaceWithConcatenatedContent.class).iwant(
				"target/copyOfSrc/as-path");

		assertEquals(pathLine("copyOfSrc") + pathLine("copyOfSrc"), out());
		assertEquals("", err());

		assertEquals("new src content\n", cachedContent("copyOfSrc"));
	}

	public void testAnotherTargetContentAndBytesConcatenated()
			throws IOException {
		new FileWriter(wsRoot() + "/src").append("src content\n").close();
		at(WorkspaceWithConcatenatedContent.class).iwant(
				"target/anotherTargetContentAndBytesConcatenated/as-path");

		assertEquals(pathLine("anotherTargetContentAndBytesConcatenated"),
				out());
		assertEquals("", err());

		assertEquals("src content\nABC\nDEF\n",
				cachedContent("anotherTargetContentAndBytesConcatenated"));
	}

	public void testAnotherTargetPathAndStringConcatenated() throws IOException {
		new FileWriter(wsRoot() + "/src").append("src content\n").close();
		at(WorkspaceWithConcatenatedContent.class).iwant(
				"target/anotherTargetPathAndStringConcatenated/as-path");

		assertEquals(pathLine("anotherTargetPathAndStringConcatenated"), out());
		assertEquals("", err());

		assertEquals("path=" + pathToCachedTarget("copyOfSrc"),
				cachedContent("anotherTargetPathAndStringConcatenated"));
	}

	/**
	 * TODO actually we shouldn't refresh, but this is so far the easiest way of
	 * ensuring scripts get rerun when paths referenced in them have been
	 * refreshed, because this way the scripts themselves get touched.
	 */
	public void testAnotherTargetPathAndStringConcatenatedIsRefreshedWhenSrcChanges()
			throws IOException, InterruptedException {
		testAnotherTargetPathAndStringConcatenated();
		long firstModTime = new File(
				pathToCachedTarget("anotherTargetPathAndStringConcatenated"))
				.lastModified();

		Thread.sleep(1000);
		startOfOutAndErrCapture();
		new FileWriter(wsRoot() + "/src").append("modified src content\n")
				.close();
		at(WorkspaceWithConcatenatedContent.class).iwant(
				"target/anotherTargetPathAndStringConcatenated/as-path");

		assertEquals(pathLine("anotherTargetPathAndStringConcatenated"), out());
		assertEquals("", err());

		// ingredient shall be fresh:
		assertEquals("modified src content\n", cachedContent("copyOfSrc"));
		// and target itself also:
		long newModTime = new File(
				pathToCachedTarget("anotherTargetPathAndStringConcatenated"))
				.lastModified();
		assertTrue(newModTime + " should have been > " + firstModTime,
				newModTime > firstModTime);
	}

	/**
	 * This tests iwant deletes the cached target before calling refresh
	 */
	public void testScriptThatCreatesDirWorksTwice() throws IOException {
		file("mkdirScriptSource").withContent().line("1").exists();
		at(WorkspaceWithShellScript.class).iwant(
				"target/mkdirScriptGeneratedContent/as-path");

		assertEquals("", err());
		assertEquals(pathLine("mkdirScriptGeneratedContent"), out());

		assertEquals("1\n",
				contentOf(pathToCachedTarget("mkdirScriptGeneratedContent")
						+ "/mkdirScriptSource"));

		// now again with a different source content:
		sleep();
		startOfOutAndErrCapture();

		file("mkdirScriptSource").withContent().line("2").exists();
		at(WorkspaceWithShellScript.class).iwant(
				"target/mkdirScriptGeneratedContent/as-path");

		assertEquals("", err());
		assertEquals(pathLine("mkdirScriptGeneratedContent"), out());

		assertEquals("2\n",
				contentOf(pathToCachedTarget("mkdirScriptGeneratedContent")
						+ "/mkdirScriptSource"));
	}

	public static class WorkspaceWithExplicitTargetCollection implements
			WorkspaceDefinition {

		public static class Root extends RootPath {

			public Root(Locations locations) {
				super(locations);
			}

			@Override
			public SortedSet<Target<?>> targets() {
				SortedSet<Target<?>> targets = new TreeSet<Target<?>>();
				targets.add(visible());
				for (int i = 0; i < 4; i++) {
					targets.add(target("dynamic-" + i).content(
							Concatenated.from()
									.string("content of dynamic-" + i).end())
							.end());
				}
				return targets;
			}

			public Target<Concatenated> visible() {
				return target("visible").content(
						Concatenated.from().string("content of visible").end())
						.end();
			}

			/**
			 * This shall not be visible
			 */
			public Target<Concatenated> invisible() {
				return target("invisible").content(
						Concatenated.from().string("content of invisible")
								.end()).end();
			}

		}

		@Override
		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testListOfTargetsFromExplicitCollectionWithDynamicTargets() {
		at(WorkspaceWithExplicitTargetCollection.class)
				.iwant("list-of/targets");
		assertEquals(
				"pout:dynamic-0\npout:dynamic-1\npout:dynamic-2\npout:dynamic-3\npout:visible\n",
				out());
		assertEquals("", err());
	}

	public void testRefreshOfDynamicTarget() throws IOException {
		at(WorkspaceWithExplicitTargetCollection.class).iwant(
				"target/dynamic-2/as-path");

		assertEquals(pathLine("dynamic-2"), out());
		assertEquals("", err());

		assertEquals("content of dynamic-2", cachedContent("dynamic-2"));
	}

	public void testRefreshOfPublicMethodTargetFailsWhenItDoesNotBelongToExplicitCollection() {
		try {
			at(WorkspaceWithExplicitTargetCollection.class).iwant(
					"target/invisible/as-path");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}

		assertEquals("", out());
		assertEquals("", err());

		assertFalse(new File(pathToCachedTarget("invisible")).exists());
	}

}
