package net.sf.iwant.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;

public class JavaClasses implements Content {

	private final Source src;
	private final List<Path> classPath = new ArrayList();
	private final SortedSet<Path> sources = new TreeSet();
	private final SortedSet<Target> dependencies = new TreeSet();

	public JavaClasses(Source src) {
		this.src = src;
		this.sources.add(src);
	}

	public static JavaClasses compiledFrom(Source src) {
		return new JavaClasses(src);
	}

	public JavaClasses using(Path classes) {
		classPath.add(classes);
		if (classes instanceof Target) {
			dependencies.add((Target) classes);
		}
		// TODO else add to sources?
		return this;
	}

	public SortedSet<Path> sources() {
		return sources;
	}

	public SortedSet<Target> dependencies() {
		return dependencies;
	}

	public void refresh(File destination) {
		destination.mkdir();
		Project project = new Project();
		project.addBuildListener(new JavacListener());
		Javac javac = new Javac();
		javac.setProject(project);
		javac.setSrcdir(antPath(project, src.name()));
		javac.setDestdir(destination);
		org.apache.tools.ant.types.Path path = javac.createClasspath();
		for (Path cpItem : classPath) {
			path.add(antPath(project, cpItem.name()));
		}
		javac.setFork(true);
		javac.setDebug(true);
		javac.execute();

	}

	private static org.apache.tools.ant.types.Path antPath(Project project,
			String name) {
		return new org.apache.tools.ant.types.Path(project, name);
	}

	private static class JavacListener implements BuildListener {

		public void buildFinished(BuildEvent e) {
		}

		public void buildStarted(BuildEvent e) {
		}

		public void messageLogged(BuildEvent e) {
			if (e.getTask() == null)
				return;
			if (!Javac.class.equals(e.getTask().getClass()))
				return;
			if (e.getPriority() > Project.MSG_WARN)
				return;
			System.err.println(e.getMessage());
		}

		public void targetFinished(BuildEvent e) {
		}

		public void targetStarted(BuildEvent e) {
		}

		public void taskFinished(BuildEvent e) {
		}

		public void taskStarted(BuildEvent e) {
		}

	}

}
