package net.sf.iwant.entry3;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.eclipsesettings.DotClasspath;

public class WsDefEclipseProjectTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File iwantApiClasses;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		iwantApiClasses = testArea.newDir("iwant-api-classes");
	}

	public void testDotProjectHasCorrectName() {
		assertEquals("a", new WsDefEclipseProject("a", "any-src",
				iwantApiClasses).dotProject().name());
		assertEquals("b", new WsDefEclipseProject("b", "any-src",
				iwantApiClasses).dotProject().name());
	}

	public void testDotClasspathHasCorrectSrcDir() {
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"i-have/wsdef\"/>\n]",
				new WsDefEclipseProject("a", "i-have/wsdef", iwantApiClasses)
						.dotClasspath().srcs().toString());
		assertEquals(
				"[        <classpathentry kind=\"src\" path=\"different-src\"/>\n]",
				new WsDefEclipseProject("a", "different-src", iwantApiClasses)
						.dotClasspath().srcs().toString());
	}

	public void testDotClasspathRefersToCachedIwantClasses() {
		DotClasspath dotClasspath = new WsDefEclipseProject("a", "any-src",
				iwantApiClasses).dotClasspath();
		assertEquals("[        <classpathentry kind=\"lib\" path=\""
				+ iwantApiClasses + "\"/>\n]", dotClasspath.deps().toString());
	}

}
