package net.sf.iwant.entry3;

import java.io.File;

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

	public void testDotProjectHasCorrectName() {
		assertEquals("a", new WorkspaceEclipseProject("a", "any-wsdefdef",
				"any-wsdef", iwantApiClasses).dotProject().name());
		assertEquals("b", new WorkspaceEclipseProject("b", "any-wsdedef",
				"any-wsdef", iwantApiClasses).dotProject().name());
	}

	public void testDotClasspathHasCorrectSrcDir() {
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"i-have/wsdefdef\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"i-have/wsdef\"/>\n]",
				new WorkspaceEclipseProject("a", "i-have/wsdefdef",
						"i-have/wsdef", iwantApiClasses).dotClasspath().srcs()
						.toString());
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"i-have/wsdefdef2\"/>\n"
						+ ",         <classpathentry kind=\"src\" path=\"i-have/wsdef2\"/>\n"
						+ "]", new WorkspaceEclipseProject("a",
						"i-have/wsdefdef2", "i-have/wsdef2", iwantApiClasses)
						.dotClasspath().srcs().toString());
	}

	public void testDotClasspathRefersToCachedIwantClasses() {
		DotClasspath dotClasspath = new WorkspaceEclipseProject("a",
				"any-wsdefdef", "any-wsdef", iwantApiClasses).dotClasspath();
		assertEquals("[        <classpathentry kind=\"lib\" path=\""
				+ iwantApiClasses + "\"/>\n]", dotClasspath.deps().toString());
	}

}
