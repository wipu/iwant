package net.sf.iwant.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public abstract class WorkspaceBuilderTestBase extends TestCase {

	private static final boolean SHOW_OUTPUT = false;

	private InputStream originalIn;

	private PrintStream originalOut;

	private PrintStream originalErr;

	private String oldPrintPrefix;

	private static final String LINE_SEPARATOR_KEY = "line.separator";

	private String originalLineSeparator;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	/**
	 * TODO find a way to make this non-static
	 */
	private static TestArea testArea;

	@Override
	public void setUp() {
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
		oldPrintPrefix = System.getProperty(PrintPrefixes.SYSTEM_PROPERTY_NAME);
		System.setProperty(PrintPrefixes.SYSTEM_PROPERTY_NAME, "p");
		testArea = TestArea.newEmpty();
	}

	protected void startOfOutAndErrCapture() {
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	protected static void ensureEmpty(String dirname) {
		FileUtils.ensureEmpty(dirname);
	}

	protected String cachedContent(String target) throws IOException {
		return testArea.cachedContent(target);
	}

	protected static String contentOf(String path) throws IOException {
		return TestArea.contentOf(path);
	}

	protected String pathLine(String target) {
		return "pout:" + pathToCachedTarget(target) + "\n";
	}

	protected String pathToCachedTarget(String target) {
		return testArea.pathToCachedTarget(target);
	}

	protected void sleep() {
		touchTwoSecondsBack(new File(testarea()));
	}

	private void touchTwoSecondsBack(File file) {
		if (".svn".equals(file.getName())) {
			return;
		}
		// save old timestamp first because modifying directory contents will
		// change it
		long oldStamp = file.lastModified();
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				touchTwoSecondsBack(child);
			}
		}
		file.setLastModified(oldStamp - 2000L);
	}

	@Override
	public void tearDown() {
		if (oldPrintPrefix == null) {
			System.clearProperty(PrintPrefixes.SYSTEM_PROPERTY_NAME);
		} else {
			System.setProperty(PrintPrefixes.SYSTEM_PROPERTY_NAME,
					oldPrintPrefix);
		}
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
		return testArea.wsRoot();
	}

	protected String cacheDir() {
		return testArea.cacheDir();
	}

	protected static String mockWeb() {
		return testArea.mockWeb();
	}

	protected String testarea() {
		return testArea.testArea();
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
			WorkspaceBuilder.build(new String[] { wsDefClassName, wsRoot(),
					wish, cacheDir() });
		}

	}

	protected <T extends WorkspaceDefinition> At at(Class<T> wsDefClass) {
		return new At(wsDefClass.getName());
	}

	protected void directoryExists(String name) {
		FileUtils.ensureDir(new File(wsRoot() + "/" + name));
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
