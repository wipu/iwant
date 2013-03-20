package net.sf.iwant.eclipsesettings;

import junit.framework.TestCase;

public class EclipseAntScriptTest extends TestCase {

	public void testGetters() {
		EclipseAntScript script = new EclipseAntScript("proj1", "..",
				"classes1", "src1", "as-proj1-developer");

		assertEquals("proj1", script.projectName());
		assertEquals("..", script.relativeBasedir());
		assertEquals("classes1", script.classesTargetName());
		assertEquals("src1", script.srcTargetName());
		assertEquals("as-proj1-developer", script.asSomeone());
	}

	public void testFileContent() {
		EclipseAntScript script = new EclipseAntScript("proj2", "../..",
				"classes2", "src2", "as-proj2-developer");

		StringBuilder expected = new StringBuilder();
		expected.append("<project name=\"proj2-eclipse-ant-build\" default=\"project-classes\" basedir=\"../..\">\n");
		expected.append("\n");
		expected.append("        <property name=\"project-name\" value=\"proj2\" />\n");
		expected.append("        <property name=\"project-parent\" location=\"${basedir}\" />\n");
		expected.append("\n");
		expected.append("        <property name=\"as-someone\" location=\"${basedir}/as-proj2-developer\" />\n");
		expected.append("\n");
		expected.append("        <property name=\"dest\" location=\"${project-parent}/${project-name}/eclipse-ant-generated\" />\n");
		expected.append("        <property name=\"classes-by-iwant\" value=\"classes2\" />\n");
		expected.append("        <property name=\"src-by-iwant\" value=\"src2\" />\n");
		expected.append("\n");
		expected.append("        <target name=\"project-classes\">\n");
		expected.append("                <echo message=\"Wanting ${classes-by-iwant}\" />\n");
		expected.append("\n");
		expected.append("                <ant dir=\"${as-someone}/with/ant/iw\" inheritall=\"false\">\n");
		expected.append("                        <property name=\"wish\" value=\"target/${classes-by-iwant}/as-path\" />\n");
		expected.append("                </ant>\n");
		expected.append("\n");
		expected.append("                <echo message=\"Working around eclipse bugs by copying the artifacts \" />\n");
		expected.append("                <echo message=\"under the project that exports them (${project-name})\" />\n");
		expected.append("\n");
		expected.append("                <delete dir=\"${dest}\" />\n");
		expected.append("                <mkdir dir=\"${dest}\" />\n");
		expected.append("                <copy todir=\"${dest}\">\n");
		expected.append("                        <fileset dir=\"${as-someone}/.i-cached/target\">\n");
		expected.append("                                <include name=\"${classes-by-iwant}/**\" />\n");
		expected.append("                                <include name=\"${src-by-iwant}/**\" />\n");
		expected.append("                        </fileset>\n");
		expected.append("                </copy>\n");
		expected.append("        </target>\n");
		expected.append("\n");
		expected.append("        <target name=\"project-clean\">\n");
		expected.append("                <delete dir=\"${dest}\" />\n");
		expected.append("        </target>\n");
		expected.append("\n");
		expected.append("</project>\n");
		assertEquals(expected.toString(), script.asFileContent());
	}

}