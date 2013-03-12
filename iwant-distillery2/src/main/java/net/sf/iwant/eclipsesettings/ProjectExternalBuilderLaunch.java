package net.sf.iwant.eclipsesettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;

public class ProjectExternalBuilderLaunch {

	private final String projectName;
	private final List<String> relativeInputPaths;
	private final String relativeOutputDirectory;

	public ProjectExternalBuilderLaunch(String projectName,
			Path generatedJavaSrc, String relativeOutputDirectory) {
		this.projectName = projectName;
		this.relativeOutputDirectory = relativeOutputDirectory;
		List<String> relativeInputPaths = new ArrayList<String>();
		for (Path ingredient : generatedJavaSrc.ingredients()) {
			if (ingredient instanceof Source) {
				relativeInputPaths.add(ingredient.name());
			}
		}
		this.relativeInputPaths = Collections
				.unmodifiableList(relativeInputPaths);
	}

	public String name() {
		return projectName;
	}

	public String relativeOutputDirectory() {
		return relativeOutputDirectory;
	}

	public List<String> relativeInputPaths() {
		return relativeInputPaths;
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		b.append("<launchConfiguration type=\"org.eclipse.ant.AntBuilderLaunchConfigurationType\">\n");
		b.append("<stringAttribute key=\"org.eclipse.ant.ui.ATTR_ANT_CLEAN_TARGETS\" value=\"project-clean,\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ant.ui.ATTR_TARGETS_UPDATED\" value=\"true\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ant.ui.DEFAULT_VM_INSTALL\" value=\"false\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;item path=&quot;/"
				+ projectName
				+ "/"
				+ relativeOutputDirectory
				+ "&quot; type=&quot;2&quot;/&gt;&#10;&lt;/resources&gt;}\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND\" value=\"false\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.CLASSPATH_PROVIDER\" value=\"org.eclipse.ant.ui.AntClasspathProvider\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.jdt.launching.DEFAULT_CLASSPATH\" value=\"true\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/CDC-1.0%Foundation-1.0\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.ant.internal.launching.remote.InternalAntRunner\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\""
				+ projectName + "\"/>\n");

		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_BUILD_SCOPE\" value=\"${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;&lt;");
		for (String inputPath : relativeInputPaths()) {
			b.append("item path=&quot;/" + inputPath
					+ "&quot; type=&quot;2&quot;/&gt;&#10;&lt;");
		}
		b.append("/resources&gt;}\"/>\n");

		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_LOCATION\" value=\"${workspace_loc:/"
				+ projectName + "/eclipse-ant-build.xml}\"/>\n");
		b.append("<stringAttribute key=\"org.eclipse.ui.externaltools.ATTR_RUN_BUILD_KINDS\" value=\"full,incremental,auto,clean\"/>\n");
		b.append("<booleanAttribute key=\"org.eclipse.ui.externaltools.ATTR_TRIGGERS_CONFIGURED\" value=\"true\"/>\n");
		b.append("<stringAttribute key=\"process_factory_id\" value=\"org.eclipse.ant.ui.remoteAntProcessFactory\"/>\n");
		b.append("</launchConfiguration>\n");
		return b.toString();
	}

}
