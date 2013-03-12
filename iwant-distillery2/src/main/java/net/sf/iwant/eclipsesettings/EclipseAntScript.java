package net.sf.iwant.eclipsesettings;

public class EclipseAntScript {

	private String projectName;
	private String relativeBasedir;
	private String classesTargetName;
	private String srcTargetName;
	private String asSomeone;

	public EclipseAntScript(String projectName, String relativeBasedir,
			String classesTargetName, String srcTargetName, String asSomeone) {
		this.projectName = projectName;
		this.relativeBasedir = relativeBasedir;
		this.classesTargetName = classesTargetName;
		this.srcTargetName = srcTargetName;
		this.asSomeone = asSomeone;
	}

	public String projectName() {
		return projectName;
	}

	public String relativeBasedir() {
		return relativeBasedir;
	}

	public String srcTargetName() {
		return srcTargetName;
	}

	public String classesTargetName() {
		return classesTargetName;
	}

	public String asSomeone() {
		return asSomeone;
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("<project name=\"" + projectName()
				+ "-eclipse-ant-build\" default=\"project-classes\" basedir=\""
				+ relativeBasedir() + "\">\n");
		b.append("\n");
		b.append("        <property name=\"project-name\" value=\""
				+ projectName() + "\" />\n");
		b.append("        <property name=\"project-parent\" location=\"${basedir}\" />\n");
		b.append("\n");
		b.append("        <property name=\"as-someone\" location=\"${basedir}/"
				+ asSomeone() + "\" />\n");
		b.append("\n");
		b.append("        <property name=\"dest\" location=\"${project-parent}/${project-name}/eclipse-ant-generated\" />\n");
		b.append("        <property name=\"classes-by-iwant\" value=\""
				+ classesTargetName() + "\" />\n");
		b.append("        <property name=\"src-by-iwant\" value=\""
				+ srcTargetName() + "\" />\n");
		b.append("\n");
		b.append("        <target name=\"project-classes\">\n");
		b.append("                <echo message=\"Wanting ${classes-by-iwant}\" />\n");
		b.append("\n");
		b.append("                <ant dir=\"${as-someone}/with/ant/iw\" inheritall=\"false\">\n");
		b.append("                        <property name=\"wish\" value=\"target/${classes-by-iwant}/as-path\" />\n");
		b.append("                </ant>\n");
		b.append("\n");
		b.append("                <echo message=\"Working around eclipse bugs by copying the artifacts \" />\n");
		b.append("                <echo message=\"under the project that exports them (${project-name})\" />\n");
		b.append("\n");
		b.append("                <delete dir=\"${dest}\" />\n");
		b.append("                <mkdir dir=\"${dest}\" />\n");
		b.append("                <copy todir=\"${dest}\">\n");
		b.append("                        <fileset dir=\"${as-someone}/.i-cached/target\">\n");
		b.append("                                <include name=\"${classes-by-iwant}/**\" />\n");
		b.append("                                <include name=\"${src-by-iwant}/**\" />\n");
		b.append("                        </fileset>\n");
		b.append("                </copy>\n");
		b.append("        </target>\n");
		b.append("\n");
		b.append("        <target name=\"project-clean\">\n");
		b.append("                <delete dir=\"${dest}\" />\n");
		b.append("        </target>\n");
		b.append("\n");
		b.append("</project>\n");
		return b.toString();
	}

}
