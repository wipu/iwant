package org.fluentjava.iwant.eclipsesettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DotProjectTest {

	private StringBuilder expected;

	@BeforeEach
	public void before() {
		expected = new StringBuilder();
	}

	@Test
	public void minimalProjectA() {
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

	@Test
	public void minimalProjectB() {
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

	@Test
	public void hasExternalBuilder() {
		assertTrue(DotProject.named("codegen").hasExternalBuilder(true).end()
				.hasExternalBuilder());
		assertFalse(DotProject.named("no-codegen").hasExternalBuilder(false)
				.end().hasExternalBuilder());
	}

	@Test
	public void externalBuilderReferenceInContent() {
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

	@Test
	public void scalaSupport() {
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

	@Test
	public void kotlinSupport() {
		DotProject dp = DotProject.named("kotlin-project")
				.hasKotlinSupport(true).end();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<projectDescription>\n");
		expected.append("        <name>kotlin-project</name>\n");
		expected.append("        <comment></comment>\n");
		expected.append("        <projects>\n");
		expected.append("        </projects>\n");
		expected.append("        <buildSpec>\n");
		expected.append("                <buildCommand>\n");
		expected.append(
				"                        <name>org.jetbrains.kotlin.ui.kotlinBuilder</name>\n");
		expected.append("                        <arguments>\n");
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
				"                <nature>org.jetbrains.kotlin.core.kotlinNature</nature>\n");
		expected.append(
				"                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		expected.append("        </natures>\n");
		expected.append("        <linkedResources>\n");
		expected.append("                <link>\n");
		expected.append("                        <name>kotlin_bin</name>\n");
		expected.append("                        <type>2</type>\n");
		expected.append(
				"                        <locationURI>org.jetbrains.kotlin.core.filesystem:/kotlin-project/kotlin_bin</locationURI>\n");
		expected.append("                </link>\n");
		expected.append("        </linkedResources>\n");
		expected.append("</projectDescription>\n");

		assertEquals(expected.toString(), dp.asFileContent());
	}

}
