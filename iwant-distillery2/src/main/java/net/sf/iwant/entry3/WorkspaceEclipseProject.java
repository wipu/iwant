package net.sf.iwant.entry3;

import java.io.File;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotProject;

/**
 * TODO make this an "eclipse project" when there is such a concept
 */
public class WorkspaceEclipseProject {

	private final String name;
	private final String wsDefdef;
	private final String wsDef;
	private final File iwantApiClasses;

	public WorkspaceEclipseProject(String name, String wsDefdef, String wsDef,
			File iwantApiClasses) {
		this.name = name;
		this.wsDefdef = wsDefdef;
		this.wsDef = wsDef;
		this.iwantApiClasses = iwantApiClasses;
	}

	public DotProject dotProject() {
		return DotProject.named(name).end();
	}

	public DotClasspath dotClasspath() {
		// TODO canonical path?
		return DotClasspath.with().src(wsDefdef).src(wsDef)
				.binDep(iwantApiClasses.getAbsolutePath()).end();
	}

}
