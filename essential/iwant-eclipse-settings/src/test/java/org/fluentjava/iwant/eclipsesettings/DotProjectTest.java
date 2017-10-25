package org.fluentjava.iwant.eclipsesettings;

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
		expected.append(
				"                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
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
		expected.append(
				"                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("</projectDescription>\n");
		assertEquals(expected.toString(), dp.asFileContent());
	}

	public void testHasExternalBuilder() {
		assertTrue(DotProject.named("codegen").hasExternalBuilder(true).end()
				.hasExternalBuilder());
		assertFalse(DotProject.named("no-codegen").hasExternalBuilder(false)
				.end().hasExternalBuilder());
	}

	public void testExternalBuilderReferenceInContent() {
		DotProject dp = DotProject.named("codegen").hasExternalBuilder(true)
				.end();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<projectDescription>\n");
		expected.append("        <name>codegen</name>\n");
		expected.append("        <comment></comment>\n");
		expected.append("        <projects>\n");
		expected.append("        </projects>\n");
		expected.append("        <buildSpec>\n");
		expected.append("                <buildCommand>\n");
		expected.append(
				"                        <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                                <dictionary>\n");
		expected.append(
				"                                        <key>LaunchConfigHandle</key>\n");
		expected.append(
				"                                        <value>&lt;project&gt;/.externalToolBuilders/codegen.launch</value>\n");
		expected.append("                                </dictionary>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("                <buildCommand>\n");
		expected.append(
				"                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("</projectDescription>\n");
		assertEquals(expected.toString(), dp.asFileContent());
	}

	public void testScalaSupport() {
		DotProject dp = DotProject.named("mixed").hasScalaSupport(true).end();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<projectDescription>\n");
		expected.append("        <name>mixed</name>\n");
		expected.append("        <comment></comment>\n");
		expected.append("        <projects>\n");
		expected.append("        </projects>\n");
		expected.append("        <buildSpec>\n");
		expected.append("                <buildCommand>\n");
		expected.append(
				"                        <name>org.scala-ide.sdt.core.scalabuilder</name>\n");
		expected.append("                        <arguments>\n");
		expected.append("                        </arguments>\n");
		expected.append("                </buildCommand>\n");
		expected.append("        </buildSpec>\n");
		expected.append("        <natures>\n");
		expected.append(
				"                <nature>org.scala-ide.sdt.core.scalanature</nature>\n");
		expected.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("</projectDescription>\n");
		assertEquals(expected.toString(), dp.asFileContent());
	}

}
