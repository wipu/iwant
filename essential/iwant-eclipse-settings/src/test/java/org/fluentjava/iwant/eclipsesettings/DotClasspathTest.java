package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DotClasspathTest {

	private StringBuilder out;

	@BeforeEach
	public void before() {
		out = new StringBuilder();
	}

	@Test
	public void minimalWithoutEvenSrc() {
		DotClasspath dp = DotClasspath.with().end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

	@Test
	public void minimalWithOnlySrc() {
		DotClasspath dp = DotClasspath.with().src("src").end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

	public void test2SourceDirsAndProjectDepAndLibraryDep() {
		DotClasspath dp = DotClasspath.with().src("src/main/java")
				.src("src/test/java").srcDep("another-project")
				.binDep("/libs/library.jar").end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append(
				"        <classpathentry kind=\"src\" path=\"src/main/java\"/>\n");
		out.append(
				"        <classpathentry kind=\"src\" path=\"src/test/java\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/another-project\"/>\n");
		out.append(
				"        <classpathentry kind=\"lib\" path=\"/libs/library.jar\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

	@Test
	public void libraryDepWithSourceAttachment() {
		DotClasspath dp = DotClasspath.with().src("src")
				.binDep("a.jar", "a-src.zip").end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry kind=\"lib\" path=\"a.jar\" sourcepath=\"a-src.zip\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

	@Test
	public void kotlinContainer() {
		DotClasspath dp = DotClasspath.with().src("src").kotlinContainer()
				.end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.jetbrains.kotlin.core.KOTLIN_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

	// code generation

	@Test
	public void exportedClassesWithSourceAttachment() {
		DotClasspath dp = DotClasspath.with().src("src")
				.exportedClasses("a.jar", "a-src.zip").end();
		out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.append("<classpath>\n");
		out.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		out.append(
				"        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		out.append(
				"        <classpathentry exported=\"true\" kind=\"lib\" path=\"a.jar\" sourcepath=\"a-src.zip\"/>\n");
		out.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		out.append("</classpath>\n");
		assertEquals(out.toString(), dp.asFileContent());
	}

}
