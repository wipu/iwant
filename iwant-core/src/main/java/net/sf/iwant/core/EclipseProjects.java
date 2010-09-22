package net.sf.iwant.core;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

public class EclipseProjects implements Content {

	private final SortedSet<Path> sources = new TreeSet();
	private final SortedSet<Target> dependencies = new TreeSet();
	private final SortedSet<EclipseProject> projects = new TreeSet();

	public static EclipseProjects with() {
		return new EclipseProjects();
	}

	public EclipseProjects project(EclipseProject project) {
		projects.add(project);
		sources.add(project.src());
		for (Path lib : project.libs()) {
			if (lib instanceof Target) {
				dependencies.add((Target) lib);
			}
		}
		return this;
	}

	public SortedSet<Path> sources() {
		return sources;
	}

	public SortedSet<Target> dependencies() {
		return dependencies;
	}

	public void refresh(File destination) throws Exception {
		for (EclipseProject project : projects) {
			File projectDir = new File(destination.getCanonicalPath() + "/"
					+ project.name());
			project.refresh(projectDir);
		}
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
