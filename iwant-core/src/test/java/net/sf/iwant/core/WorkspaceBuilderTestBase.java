package net.sf.iwant.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

public abstract class WorkspaceBuilderTestBase extends TestCase {

	private static final boolean SHOW_OUTPUT = false;

	private InputStream originalIn;

	private PrintStream originalOut;

	private PrintStream originalErr;

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private String originalLineSeparator;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	private String testarea;

	private String wsRoot;

	private String cacheDir;

	private static String mockWeb;

	public void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
		initializeTestArea();
	}

	private void initializeTestArea() {
		testarea = new File(getClass().getResource("/iwanttestarea").getPath())
				.getAbsolutePath();
		wsRoot = testarea + "/wsroot";
		ensureEmpty(wsRoot);
		cacheDir = testarea + "/iwant-cached-test";
		ensureEmpty(cacheDir);
		mockWeb = testarea + "/mock-web";
		ensureEmpty(mockWeb);

		String cpitems = testarea + "/iwant/cpitems";
		String eclipseClasses = testarea + "/../../classes";
		if (new File(eclipseClasses).exists()) {
			ensureEmpty(cpitems + "/iwant-core");
			copy(eclipseClasses, cpitems + "/iwant-core");
			copy(testarea + "/../../../iwant-lib-ant-1.7.1/ant-1.7.1.jar",
					cpitems);
			copy(testarea + "/../../../iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar",
					cpitems);
			copy(testarea + "/../../../iwant-lib-junit-3.8.1/junit-3.8.1.jar",
					cpitems);
		}
	}

	private void copy(String from, String to) {
		Copy copy = new Copy();
		File fromFile = new File(from);
		if (fromFile.isDirectory()) {
			Project project = new Project();
			copy.setProject(project);
			FileSet dirSet = new FileSet();
			dirSet.setDir(fromFile);
			dirSet.createPatternSet().setIncludes("**");
			copy.add(dirSet);
		} else {
			copy.setFile(fromFile);
		}
		copy.setTodir(new File(to));
		executeSilently(copy);
	}

	private static void executeSilently(Task task) {
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		ByteArrayOutputStream newErr = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newOut));
		System.setErr(new PrintStream(newErr));
		try {
			task.execute();
		} finally {
			System.setOut(oldOut);
			System.setErr(oldErr);
		}
	}

	protected static void ensureEmpty(String dirname) {
		File dir = new File(dirname);
		del(dir);
		ensureDir(dir);
	}

	private static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists()) {
			ensureDir(parent);
		}
		dir.mkdir();
	}

	private static void del(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				del(child);
			}
		}
		file.delete();
	}

	protected String cachedContent(String target) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				pathToCachedTarget(target)));
		StringBuilder actual = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			actual.append(line).append("\n");
		}
		return actual.toString();

	}

	protected String pathLine(String target) {
		return pathToCachedTarget(target) + "\n";
	}

	protected String pathToCachedTarget(String target) {
		return cacheDir + "/target/" + target;
	}

	/**
	 * TODO we really need mocking
	 */
	protected void sleep() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);
		if (SHOW_OUTPUT) {
			System.out.print(out());
			System.err.print(err());
		}
	}

	protected String wsRoot() {
		return wsRoot;
	}

	protected String cacheDir() {
		return cacheDir;
	}

	protected static String mockWeb() {
		return mockWeb;
	}

	protected String testarea() {
		return testarea;
	}

	protected String out() {
		return out.toString();
	}

	protected String err() {
		return err.toString();
	}

	protected class At {

		private final String wsDefClassName;

		public At(String wsDefClassName) {
			this.wsDefClassName = wsDefClassName;
		}

		public void iwant(String wish) {
			WorkspaceBuilder.main(new String[] { wsDefClassName, wsRoot(),
					wish, cacheDir() });
		}

	}

	protected <T extends WorkspaceDefinition> At at(Class<T> wsDefClass) {
		return new At(wsDefClass.getName());
	}

	protected void directoryExists(String name) {
		new File(wsRoot() + "/" + name).mkdir();
	}

	private Object unfinishedBuilder;

	protected FileStart file(String name) {
		return new FileStart(name);
	}

	protected class FileStart {

		private final String name;

		public FileStart(String name) {
			this.name = name;
		}

		public FileContent withContent() {
			return start(new FileContent(name));
		}

	}

	private <BUILDER> BUILDER start(BUILDER builder) {
		if (unfinishedBuilder != null) {
			throw new IllegalStateException("You didn't finish "
					+ unfinishedBuilder);
		}
		unfinishedBuilder = builder;
		return builder;
	}

	protected class FileContent {

		private final String name;
		private final StringBuilder content = new StringBuilder();

		public FileContent(String name) {
			this.name = name;
		}

		public FileContent line(String line) {
			content.append(line).append("\n");
			return this;
		}

		public void exists() {
			try {
				unfinishedBuilder = null;
				new FileWriter(wsRoot() + "/" + name).append(content).close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	protected void line(String line) {
		FileContent fileContent = (FileContent) unfinishedBuilder;
		fileContent.line(line);
	}

	protected void exists() {
		FileContent fileContent = (FileContent) unfinishedBuilder;
		fileContent.exists();
	}

}