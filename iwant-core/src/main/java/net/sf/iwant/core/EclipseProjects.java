package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

public class EclipseProjects implements Content {

	private final SortedSet<Target> dependencies = new TreeSet();
	private final SortedSet<EclipseProject> projects = new TreeSet();

	public static EclipseProjects with() {
		return new EclipseProjects();
	}

	public EclipseProjects project(EclipseProject project) {
		projects.add(project);
		for (Path lib : project.libs()) {
			if (lib instanceof Target) {
				dependencies.add((Target) lib);
			}
		}
		return this;
	}

	public SortedSet<Path> sources() {
		return new TreeSet();
	}

	public SortedSet<Target> dependencies() {
		return dependencies;
	}

	public void refresh(File destination) throws Exception {
		for (EclipseProject project : projects) {
			File projectDir = new File(destination.getCanonicalPath() + "/"
					+ project.name());
			project.refresh(projectDir);
			if (project.hasIwantAnt()) {
				File buildXml = new File(projectDir.getCanonicalPath()
						+ "/build.xml");
				buildXml(project, buildXml);

				File extBuilders = new File(projectDir.getCanonicalPath()
						+ "/.externalToolBuilders");
				EclipseProject.ensureDir(extBuilders);
				iwantAnt(project, new File(extBuilders.getCanonicalPath()
						+ "/iwant-ant-for-eclipse.launch"));
			}
		}
	}

	private void iwantAnt(EclipseProject project, File destination)
			throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		b.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
		b.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_AFTER_CLEAN_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_AUTO_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_MANUAL_TARGETS\" value=\"fresh-eclipse-settings,\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;");
		for (EclipseProject projectToRefresh : projects) {
			b.append("&lt;item path=&quot;/" + projectToRefresh.name()
					+ "&quot; type=&quot;4&quot;/&gt;&#10;");
		}
		b.append("&lt;/resources&gt;}\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\""
				+ project.name() + "\"/>\n");
		// TODO handle all srcs:
		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/"
				+ project.name()
				+ "/"
				+ project.srcs().get(0)
				+ "&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/"
				+ project.name() + "/build.xml}\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,incremental,auto,\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
		b.append("<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		b.append("</launchConfiguration>\n");
		new FileWriter(destination).append(b.toString()).close();
	}

	private void buildXml(EclipseProject project, File destination)
			throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("<project name=\"" + project.name()
				+ "-iwant\" default=\"list-of-targets\" basedir=\".\">\n");
		b.append("\n");
		b.append("	<property name=\"i-have\" location=\"i-have\" />\n");
		b.append("	<property file=\"${i-have}/ws-info.conf\" prefix=\"ws-info\" />\n");
		b.append("	<property name=\"ws-name\" value=\"${ws-info.WSNAME}\" />\n");
		b.append("	<property name=\"ws-root\" location=\"${i-have}/${ws-info.WSROOT}\" />\n");
		b.append("	<property name=\"wsdef-src\" location=\"${i-have}/${ws-info.WSDEF_SRC}\" />\n");
		b.append("	<property name=\"wsdef-classname\" value=\"${ws-info.WSDEF_CLASS}\" />\n");
		b.append("\n");
		b.append("	<target name=\"wishdir\">\n");
		b.append("		<property name=\"wishdir\" location=\"iwant\" />\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<target name=\"cached\" depends=\"wishdir\">\n");
		b.append("		<property name=\"cached\" location=\"${wishdir}/cached\" />\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<target name=\"my-cached\" depends=\"cached\">\n");
		b.append("		<property name=\"my-cached\" location=\"${cached}/build-xml\" />\n");
		b.append("		<mkdir dir=\"${my-cached}\" />\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<target name=\"iwant-classpath\" depends=\"cached\">\n");
		b.append("		<path id=\"iwant-classpath\">\n");
		b.append("			<pathelement location=\"${cached}/iwant/cpitems/iwant-core\" />\n");
		b.append("			<fileset dir=\"${cached}/iwant/cpitems\">\n");
		b.append("				<include name=\"*.jar\" />\n");
		b.append("			</fileset>\n");
		b.append("		</path>\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<target name=\"wsdef-classes\" depends=\"iwant-classpath, my-cached\">\n");
		b.append("		<property name=\"wsdef-classes\" location=\"${my-cached}/wsdef-classes\" />\n");
		b.append("		<mkdir dir=\"${wsdef-classes}\" />\n");
		b.append("		<javac destdir=\"${wsdef-classes}\" srcdir=\"${wsdef-src}\" classpathref=\"iwant-classpath\">\n");
		b.append("		</javac>\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<macrodef name=\"iwant\">\n");
		b.append("		<attribute name=\"target-name\" />\n");
		b.append("		<sequential>\n");
		b.append("			<java dir=\"${ws-root}\" classname=\"net.sf.iwant.core.WorkspaceBuilder\" fork=\"true\" outputproperty=\"iwant-out\" resultproperty=\"iwant-result\">\n");
		b.append("				<arg value=\"${wsdef-classname}\" />\n");
		b.append("				<arg value=\"${ws-root}\" />\n");
		b.append("				<arg value=\"@{target-name}\" />\n");
		b.append("				<arg value=\"${cached}/${ws-name}\" />\n");
		b.append("				<classpath>\n");
		b.append("					<path refid=\"iwant-classpath\" />\n");
		b.append("					<path location=\"${wsdef-classes}\" />\n");
		b.append("				</classpath>\n");
		b.append("			</java>\n");
		b.append("			<echo message=\"${iwant-out}\" />\n");
		b.append("			<condition property=\"iwant-succeeded\">\n");
		b.append("				<equals arg1=\"0\" arg2=\"${iwant-result}\" />\n");
		b.append("			</condition>\n");
		b.append("			</fail message=\"Failure\" unless=\"iwant-succeeded\" />\n");
		b.append("		</sequential>\n");
		b.append("	</macrodef>\n");
		b.append("\n");
		b.append("	<target name=\"list-of-targets\" depends=\"wsdef-classes\">\n");
		b.append("		<iwant target-name=\"list-of/targets\" />\n");
		b.append("	</target>\n");
		b.append("\n");
		b.append("	<target name=\"fresh-eclipse-settings\" depends=\"wsdef-classes\">\n");
		b.append("		<iwant target-name=\"target/eclipse-projects/as-path\" />\n");
		b.append("		<copy todir=\"${ws-root}\">\n");
		b.append("			<fileset dir=\"${iwant-out}\" includes=\"**/*\" />\n");
		b.append("		</copy>\n");
		b.append("	</target>\n");
		b.append("\n");
		for (Target publicTarget : project.publicTargetsForAnt()) {
			iwantAntTarget(b, publicTarget.nameWithoutCacheDir());
		}
		b.append("</project>\n");
		new FileWriter(destination).append(b.toString()).close();
	}

	private static void iwantAntTarget(StringBuilder b, String targetName) {
		b.append("	<target name=\"" + targetName
				+ "-as-path\" depends=\"wsdef-classes\" description=\"target/"
				+ targetName + "/as-path\">\n");
		b.append("		<iwant target-name=\"target/" + targetName
				+ "/as-path\" />\n");
		b.append("	</target>\n");
		b.append("\n");
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName() + " {\n");
		for (EclipseProject project : projects) {
			b.append("  ").append(project.definitionDescription()).append("\n");
		}
		b.append("}\n");
		return b.toString();
	}

}
