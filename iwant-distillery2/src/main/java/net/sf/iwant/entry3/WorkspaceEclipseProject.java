package net.sf.iwant.entry3;

import java.util.SortedSet;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotClasspath.DotClasspathSpex;
import net.sf.iwant.eclipsesettings.DotProject;

/**
 * TODO make this an "eclipse project" when there is such a concept
 */
public class WorkspaceEclipseProject {

	private final String name;
	private final String wsDefdef;
	private final String wsDef;
	private final SortedSet<String> classpathEntries;

	public WorkspaceEclipseProject(String name, String wsDefdef, String wsDef,
			SortedSet<String> classpathEntries) {
		this.name = name;
		this.wsDefdef = wsDefdef;
		this.wsDef = wsDef;
		this.classpathEntries = classpathEntries;
	}

	public DotProject dotProject() {
		return DotProject.named(name).end();
	}

	public DotClasspath dotClasspath() {
		DotClasspathSpex out = DotClasspath.with().src(wsDefdef).src(wsDef);
		for (String entry : classpathEntries) {
			out = out.binDep(entry);
		}
		return out.end();
	}

}
