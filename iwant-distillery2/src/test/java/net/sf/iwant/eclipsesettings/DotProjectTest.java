package net.sf.iwant.eclipsesettings;

import junit.framework.TestCase;

public class DotProjectTest extends TestCase {

	private StringBuilder expected;

	@Override
	public void setUp() {
		expected = new StringBuilder();
	}

	public void testMinimalProjectA() {
		DotProject dp = DotProject.named("a").end();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<projectDescription>\n");
		expected.append("        <name>a</name>\n");
		expected.append("        <comment></comment>\n");
		expected.append("        <projects>\n");
		expected.append("        </projects>\n");
		expected.append("        <buildSpec>\n");
		expected.append("                <buildCommand>\n");
		expected.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("</projectDescription>\n");
		assertEquals(expected.toString(), dp.asFileContent());

	}

	public void testMinimalProjectB() {
		DotProject dp = DotProject.named("b").end();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<projectDescription>\n");
		expected.append("        <name>b</name>\n");
		expected.append("        <comment></comment>\n");
		expected.append("        <projects>\n");
		expected.append("        </projects>\n");
		expected.append("        <buildSpec>\n");
		expected.append("                <buildCommand>\n");
		expected.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("</projectDescription>\n");
		assertEquals(expected.toString(), dp.asFileContent());

	}

}
