package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute;

public class JunitResult implements Content {

	private final SortedSet<Target> dependencies = new TreeSet();
	private final List<Path> classPath = new ArrayList();
	private final String testClassName;

	private JunitResult(String testClassName) {
		this.testClassName = testClassName;
	}

	public static JunitResult ofClass(String testClassName) {
		return new JunitResult(testClassName);
	}

	public SortedSet<Target> dependencies() {
		return dependencies;
	}

	public JunitResult using(SortedSet<Target> classPath) {
		this.classPath.addAll(classPath);
		dependencies.addAll(classPath);
		return this;
	}

	public JunitResult using(Path classes) {
		classPath.add(classes);
		if (classes instanceof Target) {
			dependencies.add((Target) classes);
		}
		return this;
	}

	public void refresh(File destination) throws Exception {
		Project project = new Project();
		JunitListener listener = new JunitListener();
		project.addBuildListener(listener);
		JUnitTask junit = new JUnitTask();
		junit.setProject(project);
		JUnitTest test = new JUnitTest();
		test.setName(testClassName);
		test.setTodir(destination.getParentFile());
		test.setOutfile(destination.getName());
		junit.addTest(test);
		junit.setHaltonerror(true);
		junit.setHaltonfailure(true);
		junit.setShowOutput(false);
		// junit.setPrintsummary((SummaryAttribute) EnumeratedAttribute
		// .getInstance(SummaryAttribute.class, "withOutAndErr"));
		org.apache.tools.ant.types.Path path = junit.createClasspath();
		for (Path cpItem : classPath) {
			path.add(antPath(project, cpItem.name()));
		}

		FormatterElement formatter = new FormatterElement();
		formatter.setType((TypeAttribute) TypeAttribute.getInstance(
				TypeAttribute.class, "plain"));
		formatter.setExtension("");
		junit.addFormatter(formatter);

		junit.setFork(true);
		PrintStream sysout = System.out;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// Please shut up, TestListener, whoever you are:
			System.setOut(new PrintStream(out));
			junit.execute();
		} finally {
			System.setOut(sysout);
		}
		if (!listener.failures().isEmpty()) {
			for (String failure : listener.failures())
				System.err.println(failure);
		}
	}

	private static org.apache.tools.ant.types.Path antPath(Project project,
			String name) {
		return new org.apache.tools.ant.types.Path(project, name);
	}

	private static class JunitListener implements BuildListener {

		private List<String> failures = new ArrayList();

		public void buildFinished(BuildEvent e) {
		}

		public void buildStarted(BuildEvent e) {
		}

		public synchronized void messageLogged(BuildEvent e) {
			if (e.getTask() == null)
				return;
			if (!JUnitTask.class.equals(e.getTask().getClass()))
				return;
			if (e.getPriority() <= Project.MSG_ERR)
				this.failures.add(e.getMessage());
		}

		public synchronized List<String> failures() {
			return failures;
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
