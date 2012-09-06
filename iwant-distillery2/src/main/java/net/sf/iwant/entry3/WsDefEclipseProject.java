package net.sf.iwant.entry3;

import java.io.File;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotProject;

/**
 * TODO make this an "eclipse project" when there is such a concept
 */
public class WsDefEclipseProject {

	private final String name;
	private final String srcDir;
	private final File iwantApiClasses;

	public WsDefEclipseProject(String name, String srcDir, File iwantApiClasses) {
		this.name = name;
		this.srcDir = srcDir;
		this.iwantApiClasses = iwantApiClasses;
	}

	public DotProject dotProject() {
		return DotProject.named(name).end();
	}

	public DotClasspath dotClasspath() {
		// TODO canonical path?
		return DotClasspath.with().src(srcDir)
				.binDep(iwantApiClasses.getAbsolutePath()).end();
	}

}
