package net.sf.iwant.entry3;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.sf.iwant.eclipsesettings.DotClasspath;

public class WorkspaceEclipseProjectTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File iwantApiClasses;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		iwantApiClasses = testArea.newDir("iwant-api-classes");
	}

	private static SortedSet<String> classpathEntries(String... entry) {
		return new TreeSet<String>(Arrays.asList(entry));
	}

	public void testDotProjectHasCorrectName() {
		assertEquals("a", new WorkspaceEclipseProject("a", "any-wsdefdef",
				"any-wsdef",
				classpathEntries(iwantApiClasses.getAbsolutePath()))
				.dotProject().name());
		assertEquals("b", new WorkspaceEclipseProject("b", "any-wsdedef",
				"any-wsdef",
				classpathEntries(iwantApiClasses.getAbsolutePath()))
				.dotProject().name());
	}

	public void testDotClasspathHasCorrectSrcDir() {
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"i-have/wsdefdef\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"i-have/wsdef\"/>\n]",
				new WorkspaceEclipseProject("a", "i-have/wsdefdef",
						"i-have/wsdef", classpathEntries(iwantApiClasses
								.getAbsolutePath())).dotClasspath().srcs()
						.toString());
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"i-have/wsdefdef2\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"i-have/wsdef2\"/>\n"
						+ "]",
				new WorkspaceEclipseProject("a", "i-have/wsdefdef2",
						"i-have/wsdef2", classpathEntries()).dotClasspath()
						.srcs().toString());
	}

	public void testDotClasspathRefersToGivenClasspathEntries() {
		DotClasspath dotClasspath = new WorkspaceEclipseProject("a",
				"any-wsdefdef", "any-wsdef", classpathEntries(
						iwantApiClasses.getAbsolutePath(),
						"/absolute/lib1.jar", "relative/lib2.jar"))
				.dotClasspath();

		assertTrue(dotClasspath
				.deps()
				.toString()
				.contains(
						"<classpathentry kind=\"lib\""
								+ " path=\"/absolute/lib1.jar\"/>"));
		assertTrue(dotClasspath
				.deps()
				.toString()
				.contains(
						"<classpathentry kind=\"lib\""
								+ " path=\"relative/lib2.jar\"/>"));
	}
}
