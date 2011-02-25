package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WorkspaceBuilderTest extends WorkspaceBuilderTestBase {

	public void testListOfTargetsOfEmptyWs() {
		at(EmptyWorkspace.class).iwant("list-of/targets");
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testListOfTargetsWithTwoConstantTargetFiles() {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot(),
				"list-of/targets", cacheDir() });
		assertEquals("constant2-container/constant2\nconstantOne\n", out());
		assertEquals("", err());
	}

	public void testIllegalTargetAsPath() {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithTwoConstantTargetFiles.class.getName(),
					wsRoot(), "target/illegal/as-path", cacheDir() });
			fail();
		} catch (Exception e) {
			// expected
		}
		assertEquals("", out());
		assertEquals("", err());
	}

	public void testConstantOneAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot(),
				"target/constantOne/as-path", cacheDir() });
		assertEquals(pathLine("constantOne"), out());
		assertEquals("", err());

		assertEquals("constantOne content\n", cachedContent("constantOne"));
	}

	public void testConstantTwoAsPathAndItsContent() throws IOException {
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithTwoConstantTargetFiles.class.getName(), wsRoot(),
				"target/constant2-container/constant2/as-path", cacheDir() });
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

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testGeneratedJavaClassIsNonempty() throws Exception {
		new File(wsRoot() + "/src").mkdir();
		new FileWriter(wsRoot() + "/src/Empty.java").append(
				"public class Empty {}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJavaSrcAndClasses.class.getName(), wsRoot(),
				"target/classes/as-path", cacheDir() });
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

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	/**
	 * Further java compilation testing is done in the tutorial
	 */
	public void testClassWithDependencyCompiles() throws Exception {
		new File(wsRoot() + "/src1").mkdir();
		new FileWriter(wsRoot() + "/src1/Util.java").append(
				"public class Util {}\n").close();
		new File(wsRoot() + "/src2").mkdir();
		new FileWriter(wsRoot() + "/src2/Client.java")
				.append("public class Client {"
						+ " public String foo() {return Util.class.toString();}"
						+ "}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithClassesThatDependOnOtherClasses.class.getName(),
				wsRoot(), "target/classes2/as-path", cacheDir() });
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

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testJunitResultOfFailingTest() throws Exception {
		new File(wsRoot() + "/tests").mkdir();
		new FileWriter(wsRoot() + "/tests/ATest.java").append(
				"public class ATest extends junit.framework.TestCase {"
						+ " public void testValue() {"
						+ "  assertEquals(1, AProd.value());}}\n").close();
		new File(wsRoot() + "/src").mkdir();
		new FileWriter(wsRoot() + "/src/AProd.java").append(
				"public class AProd {"
						+ " public static int value() {return 2;}}\n").close();

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot(),
				"target/testResult/as-path", cacheDir() });
		assertEquals(pathLine("testResult"), out());
		assertTrue(err().contains("ATest FAILED"));
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

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot(),
				"target/testResult/as-path", cacheDir() });
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

		WorkspaceBuilder.main(new String[] {
				WorkspaceWithJunitTests.class.getName(), wsRoot(),
				"target/testResult/as-path", cacheDir() });
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

		public ContainerPath wsRoot(Locations locations) {
			return new Root(locations);
		}

	}

	public void testDownloadFailsIfFileDoesNotExist() throws Exception {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
					"target/aDownloadedFile/as-path", cacheDir() });
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
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
					"target/aDownloadedFile/as-path", cacheDir() });
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
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
					"target/aDownloadedFileWithSha/as-path", cacheDir() });
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
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
				"target/aDownloadedFile/as-path", cacheDir() });
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
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
				"target/aDownloadedFile/as-path", cacheDir() });
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out());
		assertEquals("corrupted\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownload() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("correct\n")
				.close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
				"target/aDownloadedFile/as-path", cacheDir() });
		assertEquals(pathLine("aDownloadedFile"), out());
		assertTrue(err().contains("Getting"));
		assertEquals("correct\n", cachedContent("aDownloadedFile"));
	}

	public void testSuccessfulFirstDownloadWithSha() throws Exception {
		new FileWriter(mockWeb() + "/aFileInTheWeb").append("correct\n")
				.close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
				"target/aDownloadedFileWithSha/as-path", cacheDir() });
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
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithDownloadedContent.class.getName(), wsRoot(),
				"target/aDownloadedFile/as-path", cacheDir() });
		assertEquals(pathLine("aDownloadedFile") + pathLine("aDownloadedFile"),
				out());
		assertEquals("Getting: file:" + mockWeb() + "/aFileInTheWeb\n" + "To: "
				+ pathToCachedTarget("aDownloadedFile") + "\n", err());
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
		ensureEmpty(wsRoot() + "/a/src");
		ensureEmpty(wsRoot() + "/b/src");
		new FileWriter(wsRoot() + "/b/src/B.java").append(
				"public class B {compilationFailure}\n").close();
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithEclipseProjects.class.getName(), wsRoot(),
					"target/eclipse-projects/as-path", cacheDir() });
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
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithEclipseProjects.class.getName(), wsRoot(),
				"target/eclipse-projects/as-path", cacheDir() });
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
		bc.append("        <classpathentry kind=\"lib\" path=\"" + testarea()
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
		ensureEmpty(wsRoot() + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot()
				+ "/phase2/src/com/example/phasetwo/Phase2.java").append(
				phase2java()).close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithReferenceToNextPhase.class.getName(), wsRoot(),
				"list-of/targets", cacheDir() });
		assertEquals("", err());
		assertEquals("phase2Classes\ntargetInPhase2\n", out());
	}

	public void testPhase2TargetContent() throws IOException {
		ensureEmpty(wsRoot() + "/phase2/src/com/example/phasetwo");
		new FileWriter(wsRoot()
				+ "/phase2/src/com/example/phasetwo/Phase2.java").append(
				phase2java()).close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithReferenceToNextPhase.class.getName(), wsRoot(),
				"target/targetInPhase2/as-path", cacheDir() });
		assertEquals("", err());
		assertEquals(pathLine("targetInPhase2"), out());
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
				WorkspaceWithShellScript.class.getName(), wsRoot(),
				"target/successfulScriptOutput/as-path", cacheDir() });

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("Standard out:\n");
		expectedErr.append("this was printed to stdout\n");
		expectedErr.append("DEST="
				+ pathToCachedTarget("successfulScriptOutput") + "\n");
		expectedErr.append("pwd is " + cacheDir()
				+ "/tmp-for-the-only-worker-thread\n");
		expectedErr.append("Standard err:\n");
		expectedErr.append("this was printed to stderr\n");

		assertEquals(pathLine("successfulScriptOutput"), out());
		assertEquals(expectedErr.toString(), err());

		assertEquals("hello from script\n",
				cachedContent("successfulScriptOutput"));
	}

	public void testFailingShellScript() {
		try {
			WorkspaceBuilder.main(new String[] {
					WorkspaceWithShellScript.class.getName(), wsRoot(),
					"target/failingScriptOutput/as-path", cacheDir() });
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

		assertEquals("", out());
		assertEquals(expectedErr.toString(), err());

		assertFalse(new File(pathToCachedTarget("failingScriptOutput"))
				.exists());
	}

	public void testConcatenatedSrc() throws IOException {
		new FileWriter(wsRoot() + "/src").append("src content\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithConcatenatedContent.class.getName(), wsRoot(),
				"target/copyOfSrc/as-path", cacheDir() });

		assertEquals(pathLine("copyOfSrc"), out());
		assertEquals("", err());

		assertEquals("src content\n", cachedContent("copyOfSrc"));
	}

	public void testConcatenatedSrcAfterSrcChanges() throws IOException {
		testConcatenatedSrc();
		sleep();
		new FileWriter(wsRoot() + "/src").append("new src content\n").close();
		WorkspaceBuilder.main(new String[] {
				WorkspaceWithConcatenatedContent.class.getName(), wsRoot(),
				"target/copyOfSrc/as-path", cacheDir() });

		assertEquals(pathLine("copyOfSrc") + pathLine("copyOfSrc"), out());
		assertEquals("", err());

		assertEquals("new src content\n", cachedContent("copyOfSrc"));
	}

	public void testConcatenatedTargetAndBytesAndString() throws IOException {
		new FileWriter(wsRoot() + "/src").append("src content\n").close();
		WorkspaceBuilder
				.main(new String[] {
						WorkspaceWithConcatenatedContent.class.getName(),
						wsRoot(),
						"target/anotherTargetAndBytesConcatenated/as-path",
						cacheDir() });

		assertEquals(pathLine("anotherTargetAndBytesConcatenated"), out());
		assertEquals("", err());

		assertEquals("src content\nABC\nDEF\n",
				cachedContent("anotherTargetAndBytesConcatenated"));
	}

}
