package net.sf.iwant.core;

import java.util.ArrayList;
import java.util.Collection;
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
	private final SortedSet<Path> ingredients = new TreeSet();

	public JavaClasses(Source src) {
		this.src = src;
		this.ingredients.add(src);
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getName()).append(" {\n");
		b.append("  src:").append(src).append("\n");
		b.append("  classPath:" + classPath).append("\n");
		b.append("}\n");
		return b.toString();
	}

	public static JavaClasses compiledFrom(Source src) {
		return new JavaClasses(src);
	}

	public JavaClasses using(Path classes) {
		classPath.add(classes);
		ingredients.add(classes);
		return this;
	}

	public JavaClasses using(Collection<Path> classesPaths) {
		for (Path classesPath : classesPaths) {
			using(classesPath);
		}
		return this;
	}

	public List<Path> classpathItems() {
		return classPath;
	}

	public SortedSet<Path> ingredients() {
		return ingredients;
	}

	public void refresh(RefreshEnvironment refresh) {
		refresh.destination().mkdir();
		Project project = new Project();
		project.addBuildListener(new JavacListener());
		Javac javac = new Javac();
		javac.setProject(project);
		javac.setSrcdir(antPath(project,
				src.asAbsolutePath(refresh.locations())));
		javac.setDestdir(refresh.destination());
		org.apache.tools.ant.types.Path path = javac.createClasspath();
		for (Path cpItem : classPath) {
			path.add(antPath(project,
					cpItem.asAbsolutePath(refresh.locations())));
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
			// not interested
		}

		public void buildStarted(BuildEvent e) {
			// not interested
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
			// not interested
		}

		public void targetStarted(BuildEvent e) {
			// not interested
		}

		public void taskFinished(BuildEvent e) {
			// not interested
		}

		public void taskStarted(BuildEvent e) {
			// not interested
		}

	}

}
