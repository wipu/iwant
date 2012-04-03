package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement;
import org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTask;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.types.EnumeratedAttribute;

public class JunitResult implements Content {

	private final SortedSet<Path> ingredients = new TreeSet<Path>();
	private final List<Path> classPath = new ArrayList<Path>();
	private final String testClassName;

	private JunitResult(String testClassName) {
		this.testClassName = testClassName;
	}

	@Override
	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getName()).append(" {\n");
		b.append("  testClassName:").append(testClassName).append("\n");
		b.append("  classPath:" + classPath).append("\n");
		b.append("}\n");
		return b.toString();
	}

	public static JunitResult ofClass(String testClassName) {
		return new JunitResult(testClassName);
	}

	@Override
	public SortedSet<Path> ingredients() {
		return ingredients;
	}

	public JunitResult using(Collection<? extends Path> classPath) {
		this.classPath.addAll(classPath);
		ingredients.addAll(classPath);
		return this;
	}

	public JunitResult using(Path classes) {
		classPath.add(classes);
		ingredients.add(classes);
		return this;
	}

	@Override
	public void refresh(RefreshEnvironment refresh) throws Exception {
		Project project = new Project();
		JunitListener listener = new JunitListener();
		project.addBuildListener(listener);
		JUnitTask junit = new JUnitTask();
		junit.setProject(project);
		JUnitTest test = new JUnitTest();
		test.setName(testClassName);
		test.setTodir(refresh.destination().getParentFile());
		test.setOutfile(refresh.destination().getName());
		junit.addTest(test);
		junit.setHaltonerror(true);
		junit.setHaltonfailure(true);
		junit.setShowOutput(false);
		// junit.setPrintsummary((SummaryAttribute) EnumeratedAttribute
		// .getInstance(SummaryAttribute.class, "withOutAndErr"));
		org.apache.tools.ant.types.Path path = junit.createClasspath();
		for (Path cpItem : classPath) {
			path.add(antPath(project,
					cpItem.asAbsolutePath(refresh.locations())));
		}

		FormatterElement formatter = new FormatterElement();
		formatter.setType((TypeAttribute) EnumeratedAttribute.getInstance(
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
			for (String failure : listener.failures()) {
				String prefixedFailure = PrintPrefixes.fromSystemProperty()
						.multiLineErr(failure);
				TextOutput.debugLog("Warning user about test failure: "
						+ prefixedFailure);
				System.err.println(prefixedFailure);
			}
		}
	}

	private static org.apache.tools.ant.types.Path antPath(Project project,
			String name) {
		return new org.apache.tools.ant.types.Path(project, name);
	}

	private static class JunitListener implements BuildListener {

		private List<String> failures = new ArrayList<String>();

		@Override
		public void buildFinished(BuildEvent e) {
			// not interested
		}

		@Override
		public void buildStarted(BuildEvent e) {
			// not interested
		}

		@Override
		public synchronized void messageLogged(BuildEvent e) {
			if (e.getTask() == null) {
				return;
			}
			if (!JUnitTask.class.equals(e.getTask().getClass())) {
				return;
			}
			if (e.getPriority() <= Project.MSG_ERR) {
				this.failures.add(e.getMessage());
				TextOutput.debugLog("Junit test failed: " + e.getMessage());
			}
		}

		public synchronized List<String> failures() {
			return failures;
		}

		@Override
		public void targetFinished(BuildEvent e) {
			// not interested
		}

		@Override
		public void targetStarted(BuildEvent e) {
			// not interested
		}

		@Override
		public void taskFinished(BuildEvent e) {
			// not interested
		}

		@Override
		public void taskStarted(BuildEvent e) {
			// not interested
		}

	}

}
